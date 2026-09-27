package com.qa.framework.driver;

import com.qa.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;

/**
 * Creates local or remote (Selenium Grid) browsers. Driver binaries are resolved automatically by
 * Selenium Manager, so no WebDriverManager / driver .exe files are needed.
 */
public final class DriverFactory {

    private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver createDriver(String browserName) {
        BrowserType browser = BrowserType.from(browserName);
        boolean headless = ConfigManager.getBoolean(KEY_HEADLESS, DEFAULT_HEADLESS);
        String gridUrl = ConfigManager.get(KEY_GRID_URL);

        MutableCapabilities options = buildOptions(browser, headless);
        LOG.info("Starting {} (headless={}, grid={})", browser, headless, gridUrl == null ? "local" : gridUrl);

        WebDriver driver;
        if (gridUrl != null) {
            try {
                driver = new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid gridUrl: " + gridUrl, e);
            }
        } else {
            driver = switch (browser) {
                case CHROME -> new ChromeDriver((ChromeOptions) options);
                case FIREFOX -> new FirefoxDriver((FirefoxOptions) options);
                case EDGE -> new EdgeDriver((EdgeOptions) options);
            };
        }

        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getInt(KEY_TIMEOUT_PAGE_LOAD, DEFAULT_PAGE_LOAD_TIMEOUT_SEC)));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(ConfigManager.getInt(KEY_TIMEOUT_SCRIPT, DEFAULT_SCRIPT_TIMEOUT_SEC)));
        if (!headless) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    private static MutableCapabilities buildOptions(BrowserType browser, boolean headless) {
        String windowSize = ConfigManager.get(KEY_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        PageLoadStrategy strategy = PageLoadStrategy.valueOf(
                ConfigManager.get(KEY_PAGE_LOAD_STRATEGY, DEFAULT_PAGE_LOAD_STRATEGY).toUpperCase(Locale.ROOT));

        switch (browser) {
            case FIREFOX: {
                FirefoxOptions o = new FirefoxOptions();
                o.setPageLoadStrategy(strategy);
                String[] wh = windowSize.split(",");
                if (headless) {
                    o.addArguments(ARG_FIREFOX_HEADLESS);
                }
                o.addArguments(ARG_FIREFOX_WIDTH + wh[0].trim(), ARG_FIREFOX_HEIGHT + wh[1].trim());
                return o;
            }
            case EDGE: {
                EdgeOptions o = new EdgeOptions();
                o.setPageLoadStrategy(strategy);
                o.addArguments(commonChromiumArgs(headless, windowSize));
                o.setCapability(CAP_EDGE_LOGGING_PREFS, browserConsoleLogging());
                return o;
            }
            case CHROME:
            default: {
                ChromeOptions o = new ChromeOptions();
                o.setPageLoadStrategy(strategy);
                o.addArguments(commonChromiumArgs(headless, windowSize));
                o.setCapability(CAP_CHROME_LOGGING_PREFS, browserConsoleLogging());
                return o;
            }
        }
    }

    /** Lets FailureEvidence read the browser console (Chrome / Edge; Firefox does not support it). */
    private static LoggingPreferences browserConsoleLogging() {
        LoggingPreferences prefs = new LoggingPreferences();
        prefs.enable(LogType.BROWSER, Level.ALL);
        return prefs;
    }

    private static java.util.List<String> commonChromiumArgs(boolean headless, String windowSize) {
        java.util.List<String> args = new java.util.ArrayList<>(java.util.List.of(
                ARG_WINDOW_SIZE + windowSize,
                ARG_DISABLE_NOTIFICATIONS,
                ARG_DISABLE_SEARCH_ENGINE_CHOICE,
                ARG_NO_SANDBOX,          // required in most Linux containers / CI agents
                ARG_DISABLE_DEV_SHM));   // avoids /dev/shm crashes in Docker
        if (headless) {
            args.add(ARG_CHROMIUM_HEADLESS);
            args.add(ARG_DISABLE_GPU);
        }
        return args;
    }
}
