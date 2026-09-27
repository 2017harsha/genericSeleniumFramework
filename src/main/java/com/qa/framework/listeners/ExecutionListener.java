package com.qa.framework.listeners;

import com.qa.framework.reporting.FailureEvidence;
import org.testng.IExecutionListener;

/**
 * Runs exactly once per TestNG execution (mvn test, Jenkins build, or an IntelliJ run),
 * before any suite starts: wipes the previous run's failure-evidence folder and creates a fresh one.
 *
 * Registered automatically via META-INF/services/org.testng.ITestNGListener.
 */
public class ExecutionListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        FailureEvidence.resetForNewRun();
    }
}
