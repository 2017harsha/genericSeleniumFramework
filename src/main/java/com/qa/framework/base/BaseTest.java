package com.qa.framework.base;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.driver.DriverFactory;
import com.qa.framework.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Extend this in every UI test class. A fresh browser is opened before and closed after each
 * test method, so methods are fully independent and can run in parallel.
 *
 * <p>Browser precedence: {@code <parameter name="browser">} in the suite XML (for cross-browser
 * suites) &gt; {@code -Dbrowser} / env var BROWSER &gt; config file.
 */
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    @Parameters(PARAM_BROWSER)
    public void setUpDriver(@Optional String browserFromXml) {
        String browser = browserFromXml != null ? browserFromXml : ConfigManager.get(KEY_BROWSER, DEFAULT_BROWSER);
        DriverManager.setDriver(DriverFactory.createDriver(browser));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        DriverManager.quitDriver();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }
}
