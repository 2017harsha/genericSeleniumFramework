package com.qa.framework.listeners;

import com.qa.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAlterSuiteListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static com.qa.framework.base.ConstantTest.*;

/**
 * 1. Lets Jenkins / command line control parallelism without editing XML:
 *    {@code -Dparallel=methods|classes|tests|none -Dthreads=4}
 * 2. Writes Allure's environment.properties so the report shows env / browser / headless.
 *
 * Registered automatically via META-INF/services/org.testng.ITestNGListener.
 */
public class SuiteConfigListener implements IAlterSuiteListener, ISuiteListener {

    private static final Logger LOG = LogManager.getLogger(SuiteConfigListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        String parallel = System.getProperty(KEY_PARALLEL);
        String threads = System.getProperty(KEY_THREADS);
        for (XmlSuite suite : suites) {
            if (parallel != null && !parallel.isBlank() && !SUITE_DEFAULT.equalsIgnoreCase(parallel.trim())) {
                String p = parallel.trim().toUpperCase(Locale.ROOT);
                suite.setParallel("FALSE".equals(p) ? XmlSuite.ParallelMode.NONE : XmlSuite.ParallelMode.valueOf(p));
            }
            if (threads != null && !threads.isBlank() && !SUITE_DEFAULT.equalsIgnoreCase(threads.trim())) {
                int n = Integer.parseInt(threads.trim());
                suite.setThreadCount(n);
                suite.setDataProviderThreadCount(n);
            }
            LOG.info("Suite '{}' -> parallel={}, threads={}", suite.getName(), suite.getParallel(), suite.getThreadCount());
        }
    }

    @Override
    public void onStart(ISuite suite) {
        writeAllureEnvironment();
    }

    private void writeAllureEnvironment() {
        Path dir = Paths.get(System.getProperty(KEY_ALLURE_RESULTS_DIR, DEFAULT_ALLURE_RESULTS_DIR));
        Properties p = new Properties();
        p.setProperty("Environment", ConfigManager.env());
        p.setProperty("Base.URL", ConfigManager.get(KEY_APP_URL, "-"));
        p.setProperty("Browser", ConfigManager.get(KEY_BROWSER, DEFAULT_BROWSER));
        p.setProperty("Headless", ConfigManager.get(KEY_HEADLESS, String.valueOf(DEFAULT_HEADLESS)));
        p.setProperty("Parallel", System.getProperty(KEY_PARALLEL, "-") + " / threads=" + System.getProperty(KEY_THREADS, "-"));
        p.setProperty("Grid", ConfigManager.get(KEY_GRID_URL, "local"));
        p.setProperty("Java", System.getProperty("java.version"));
        p.setProperty("OS", System.getProperty("os.name"));
        try {
            Files.createDirectories(dir);
            try (Writer w = Files.newBufferedWriter(dir.resolve(ALLURE_ENV_FILE), StandardCharsets.UTF_8)) {
                p.store(w, "Allure environment");
            }
        } catch (IOException e) {
            LOG.warn("Could not write Allure environment.properties: {}", e.getMessage());
        }
    }
}
