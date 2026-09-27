package com.qa.framework.reporting;

import com.qa.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Writes one folder per failed test with everything needed to debug it:
 * <pre>
 * failure-evidence/
 *   run-info.json                  when/where this run happened
 *   failures-summary.txt           one line per failure
 *   001_DummyFormTest.fillFormDataDriven_chrome_134402-517/
 *     screenshot.png
 *     page-source.html
 *     failure-info.json            test, params, env, browser, URL, title, timings, error
 *     stacktrace.txt
 *     browser-console.log          Chrome / Edge only
 * </pre>
 * Written only for tests that failed on EVERY attempt (1 run + retry.count retries); attempts that
 * fail and are then retried do not create a folder.
 * The root folder is deleted and recreated at the start of every run ({@link #resetForNewRun()}).
 * Thread-safe: folders are uniquely numbered, so parallel failures never collide.
 */
public final class FailureEvidence {

    private static final Logger LOG = LogManager.getLogger(FailureEvidence.class);
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final DateTimeFormatter FOLDER_TIME = DateTimeFormatter.ofPattern(FAILURE_TIME_PATTERN);
    private static final DateTimeFormatter ISO_TIME = DateTimeFormatter.ofPattern(ISO_TIME_PATTERN);
    private static final Object SUMMARY_LOCK = new Object();

    private FailureEvidence() {
    }

    public static Path rootDir() {
        return Paths.get(ConfigManager.get(KEY_FAILURE_DIR, DEFAULT_FAILURE_DIR)).toAbsolutePath();
    }

    // ------------------------------------------------------------------ run start

    /** Deletes the previous run's evidence and creates an empty root folder + run-info.json. */
    public static void resetForNewRun() {
        Path root = rootDir();
        SEQUENCE.set(0);
        try {
            if (ConfigManager.getBoolean(KEY_FAILURE_CLEAN, DEFAULT_FAILURE_CLEAN)) {
                deleteRecursively(root);
            }
            Files.createDirectories(root);

            Map<String, Object> run = new LinkedHashMap<>();
            run.put("startedAt", now());
            run.put("environment", ConfigManager.env());
            run.put("baseUrl", ConfigManager.get(KEY_APP_URL, "-"));
            run.put("browser", ConfigManager.get(KEY_BROWSER, DEFAULT_BROWSER));
            run.put("headless", ConfigManager.get(KEY_HEADLESS, String.valueOf(DEFAULT_HEADLESS)));
            run.put("grid", ConfigManager.get(KEY_GRID_URL, "local"));
            run.put("parallel", System.getProperty(KEY_PARALLEL, "-"));
            run.put("threads", System.getProperty(KEY_THREADS, "-"));
            run.put("java", System.getProperty("java.version"));
            run.put("os", System.getProperty("os.name"));
            write(root.resolve(RUN_INFO_FILE), new Json().toJson(run));
            LOG.info("Failure evidence folder ready: {}", root);
        } catch (IOException e) {
            LOG.warn("Could not prepare failure evidence folder {}: {}", root, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ per failure

    /**
     * Captures evidence for a test that has FINALLY failed (all retry attempts used up).
     * Every step is independent, so one failing capture (e.g. browser already crashed) never
     * prevents the others.
     *
     * @param attempts      how many times the test ran (1 + retries)
     * @param attemptErrors one short error line per failed attempt, oldest first
     * @return the created folder, or null if it could not be created
     */
    public static Path capture(ITestResult result, WebDriver driver, int attempts, List<String> attemptErrors) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        String method = result.getMethod().getMethodName();
        String browser = browserName(driver);
        int seq = SEQUENCE.incrementAndGet();

        String folderName = String.format(FAILURE_FOLDER_FORMAT, seq, className, method, browser,
                ZonedDateTime.now().format(FOLDER_TIME));
        Path dir = rootDir().resolve(folderName);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOG.warn("Could not create {}: {}", dir, e.getMessage());
            return null;
        }

        String url = null;
        String title = null;
        if (driver != null) {
            safe("screenshot", () -> Files.write(dir.resolve(FAILURE_SCREENSHOT_FILE),
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)));
            safe("page source", () -> write(dir.resolve(FAILURE_PAGE_SOURCE_FILE), driver.getPageSource()));
            safe("browser console", () -> writeConsoleLog(driver, dir.resolve(FAILURE_CONSOLE_LOG_FILE)));
            url = safeGet(driver::getCurrentUrl);
            title = safeGet(driver::getTitle);
        }

        Throwable error = result.getThrowable();
        safe("stack trace", () -> write(dir.resolve(FAILURE_STACKTRACE_FILE), stackTrace(error)));

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("sequence", seq);
        info.put("testClass", result.getTestClass().getRealClass().getName());
        info.put("testMethod", method);
        info.put("parameters", Arrays.stream(result.getParameters()).map(String::valueOf).toList());
        info.put("groups", Arrays.asList(result.getMethod().getGroups()));
        info.put("environment", ConfigManager.env());
        info.put("browser", browser);
        info.put("browserVersion", browserVersion(driver));
        info.put("url", url);
        info.put("pageTitle", title);
        info.put("attempts", attempts);
        info.put("attemptErrors", attemptErrors);
        info.put("thread", Thread.currentThread().getName());
        info.put("startedAt", format(result.getStartMillis()));
        info.put("failedAt", now());
        info.put("durationMs", System.currentTimeMillis() - result.getStartMillis());
        info.put("exception", error == null ? null : error.getClass().getName());
        info.put("message", error == null ? null : error.getMessage());
        info.put("files", listFiles(dir));
        safe("failure info", () -> write(dir.resolve(FAILURE_INFO_FILE), new Json().toJson(info)));

        appendSummary(seq, className + "." + method, browser, attempts, error, folderName);
        LOG.error("Failure evidence saved: {}", dir);
        return dir;
    }

    // ------------------------------------------------------------------ helpers

    private static void writeConsoleLog(WebDriver driver, Path file) throws IOException {
        List<LogEntry> entries = driver.manage().logs().get(LogType.BROWSER).getAll();
        StringBuilder sb = new StringBuilder();
        for (LogEntry e : entries) {
            sb.append(format(e.getTimestamp())).append(' ')
              .append(e.getLevel()).append(' ')
              .append(e.getMessage()).append(System.lineSeparator());
        }
        write(file, sb.length() == 0 ? "(no console messages)" : sb.toString());
    }

    /** Short one-line description of an error, used for per-attempt history. */
    public static String describe(Throwable error) {
        return error == null ? "-" : error.getClass().getSimpleName() + ": " + firstLine(error.getMessage());
    }

    private static void appendSummary(int seq, String test, String browser, int attempts, Throwable error,
                                      String folder) {
        String line = String.format("%03d | %s | %s | %s | failed %d/%d attempts | %s%n", seq, now(), test,
                browser, attempts, attempts,
                error == null ? "-" : error.getClass().getSimpleName() + ": " + firstLine(error.getMessage()))
                + "      -> " + folder + System.lineSeparator();
        synchronized (SUMMARY_LOCK) {
            safe("summary", () -> Files.writeString(rootDir().resolve(FAILURE_SUMMARY_FILE), line,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        }
    }

    private static String browserName(WebDriver driver) {
        Capabilities caps = capabilities(driver);
        String name = caps == null ? ConfigManager.get(KEY_BROWSER, DEFAULT_BROWSER) : caps.getBrowserName();
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    private static String browserVersion(WebDriver driver) {
        Capabilities caps = capabilities(driver);
        return caps == null ? null : caps.getBrowserVersion();
    }

    private static Capabilities capabilities(WebDriver driver) {
        return driver instanceof HasCapabilities hc ? hc.getCapabilities() : null;
    }

    private static List<String> listFiles(Path dir) {
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.map(p -> p.getFileName().toString()).sorted().forEach(names::add);
        } catch (IOException ignored) {
            // informational only
        }
        names.add(FAILURE_INFO_FILE);
        return names;
    }

    private static String stackTrace(Throwable t) {
        if (t == null) {
            return "(no exception)";
        }
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String firstLine(String s) {
        if (s == null) {
            return "";
        }
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl).trim();
    }

    private static String now() {
        return ZonedDateTime.now().format(ISO_TIME);
    }

    private static String format(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(ISO_TIME);
    }

    private static void write(Path file, String content) throws IOException {
        Files.writeString(file, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws Exception;
    }

    private static void safe(String what, IoAction action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warn("Could not capture {}: {}", what, e.getMessage());
        }
    }

    private static String safeGet(java.util.function.Supplier<String> s) {
        try {
            return s.get();
        } catch (Exception e) {
            return null;
        }
    }
}
