package com.qa.framework.driver;

import org.openqa.selenium.WebDriver;

/**
 * One WebDriver per test thread. This is what makes parallel TestNG execution safe:
 * every page object asks DriverManager for "my thread's" driver instead of sharing a static one.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver for thread " + Thread.currentThread().getName()
                    + ". Does your test class extend BaseTest?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void setDriver(WebDriver driver) {
        DRIVER.set(driver);
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
