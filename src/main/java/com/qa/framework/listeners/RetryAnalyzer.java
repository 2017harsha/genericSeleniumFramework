package com.qa.framework.listeners;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.reporting.FailureEvidence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Re-runs a failed test up to {@code retry.count} extra times (default 2 -> 1 run + 2 retries = 3 attempts).
 * The test is reported as FAILED only when the last attempt also fails; earlier failed attempts are
 * reported by TestNG as "retried" (skipped).
 *
 * <p>TestNG keeps one instance per test method (and per data-provider row), so the counter below is
 * the attempt count for that specific test.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);
    private final int maxRetries = Math.max(0, ConfigManager.getInt(KEY_RETRY_COUNT, DEFAULT_RETRY_COUNT));
    private int retriesUsed = 0;
    private final List<String> attemptErrors = new ArrayList<>();

    @Override
    public boolean retry(ITestResult result) {
        if (willRetry()) {
            retriesUsed++;
            LOG.warn("Attempt {}/{} of {} failed - retrying", retriesUsed, totalAttempts(),
                    result.getMethod().getMethodName());
            return true;
        }
        return false;
    }

    /** True if the attempt that just failed will be re-run (i.e. it is NOT the final failure). */
    public boolean willRetry() {
        return retriesUsed < maxRetries;
    }

    /** 1-based number of the attempt currently running / just finished. */
    public int currentAttempt() {
        return retriesUsed + 1;
    }

    public int totalAttempts() {
        return maxRetries + 1;
    }

    /** Remembers why the current attempt failed (shown in failure-info.json of the final failure). */
    public void recordFailure(Throwable error) {
        attemptErrors.add("attempt " + currentAttempt() + ": " + FailureEvidence.describe(error));
    }

    public List<String> attemptErrors() {
        return List.copyOf(attemptErrors);
    }
}
