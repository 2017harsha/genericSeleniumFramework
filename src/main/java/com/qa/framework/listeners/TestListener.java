package com.qa.framework.listeners;

import com.qa.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.qa.framework.reporting.FailureEvidence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Logs test lifecycle and, when a test has failed on its FINAL attempt (see {@link RetryAnalyzer}),
 * saves a per-test evidence folder (screenshot, page source,
 * failure-info.json, stack trace, browser console) via {@link FailureEvidence} and attaches the
 * same files to the Allure report. Runs in afterInvocation (before @AfterMethod quits the
 * browser), so the browser is still alive and the attachments land on the failed test itself.
 *
 * Registered automatically via META-INF/services/org.testng.ITestNGListener.
 */
public class TestListener implements ITestListener, IInvokedMethodListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        // only real failures (TestNG retries FAILURE only; skips are not retried and get no evidence)
        if (!method.isTestMethod() || result.getStatus() != ITestResult.FAILURE || result.getThrowable() == null) {
            return;
        }
        WebDriver driver = DriverManager.hasDriver() ? DriverManager.getDriver() : null;

        // Work out whether this failed attempt will be retried. afterInvocation runs BEFORE TestNG asks
        // the RetryAnalyzer, so we ask it ourselves (without consuming a retry).
        int attempt = 1;
        int totalAttempts = 1;
        boolean finalFailure = true;
        List<String> attemptErrors = List.of(FailureEvidence.describe(result.getThrowable()));
        IRetryAnalyzer analyzer = result.getMethod().getRetryAnalyzer(result);
        if (analyzer instanceof RetryAnalyzer retry) {
            retry.recordFailure(result.getThrowable());
            attempt = retry.currentAttempt();
            totalAttempts = retry.totalAttempts();
            finalFailure = !retry.willRetry();
            attemptErrors = retry.attemptErrors();
        }

        if (!finalFailure) {
            // intermediate attempt: no evidence folder, just a screenshot on this attempt in Allure
            LOG.warn("Attempt {}/{} of {} failed ({}) - will retry, no failure evidence saved yet",
                    attempt, totalAttempts, name(result), FailureEvidence.describe(result.getThrowable()));
            attachScreenshot(driver);
            return;
        }

        // final failure (all attempts failed): one folder under failure-evidence/ ...
        Path dir = FailureEvidence.capture(result, driver, attempt, attemptErrors);

        // ... and the same files attached to the Allure test
        if (dir != null) {
            attach(dir.resolve(FAILURE_SCREENSHOT_FILE), ATTACH_SCREENSHOT, MIME_PNG, EXT_PNG);
            attach(dir.resolve(FAILURE_PAGE_SOURCE_FILE), ATTACH_PAGE_SOURCE, MIME_HTML, EXT_HTML);
            attach(dir.resolve(FAILURE_INFO_FILE), ATTACH_FAILURE_INFO, MIME_JSON, EXT_JSON);
            attach(dir.resolve(FAILURE_CONSOLE_LOG_FILE), ATTACH_CONSOLE_LOG, MIME_TEXT, EXT_TXT);
        }
        if (driver != null) {
            try {
                Allure.addAttachment(ATTACH_URL, driver.getCurrentUrl());
            } catch (Exception e) {
                LOG.warn("Could not read current URL: {}", e.getMessage());
            }
        }
    }

    private static void attachScreenshot(WebDriver driver) {
        if (driver == null) {
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(ATTACH_SCREENSHOT, MIME_PNG, new ByteArrayInputStream(png), EXT_PNG);
        } catch (Exception e) {
            LOG.warn("Could not take screenshot: {}", e.getMessage());
        }
    }

    private static void attach(Path file, String name, String mime, String ext) {
        if (!Files.exists(file)) {
            return;
        }
        try (InputStream in = Files.newInputStream(file)) {
            Allure.addAttachment(name, mime, in, ext);
        } catch (IOException e) {
            LOG.warn("Could not attach {} to Allure: {}", file.getFileName(), e.getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START  {} [{}]", name(result), Thread.currentThread().getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS   {}", name(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL   {} -> {}", name(result), String.valueOf(result.getThrowable()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP   {}", name(result));
    }

    private static String name(ITestResult r) {
        return r.getTestClass().getRealClass().getSimpleName() + "." + r.getMethod().getMethodName();
    }
}
