package com.qa.framework.base;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchShadowRootException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;

/**
 * Parent of every page object. Wraps the common Selenium interactions with explicit waits,
 * scrolling and a JS-click fallback so page classes stay short and readable.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getInt(KEY_TIMEOUT_EXPLICIT, DEFAULT_EXPLICIT_WAIT_SEC)));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    // ---------- navigation ----------

    protected void open(String url) {
        log.info("Navigating to {}", url);
        driver.get(url);
        disableSmoothScroll();
    }

    /** Makes all scrolling instant on the current page (see ConstantPages.JS_DISABLE_SMOOTH_SCROLL). */
    protected void disableSmoothScroll() {
        ((JavascriptExecutor) driver).executeScript(JS_DISABLE_SMOOTH_SCROLL);
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---------- finding ----------

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> allVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean waitForInvisibility(By locator) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ---------- actions ----------

    protected void click(By locator) {
        WebElement el = clickable(locator);
        scrollIntoView(el);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted on {} - retrying with JavaScript", locator);
            jsClick(el);
        }
    }

    protected void type(By locator, String text) {
        WebElement el = visible(locator);
        scrollIntoView(el);
        focus(el, locator);   // some fields (e.g. readonly-until-focus) need focus first
        el.clear();
        el.sendKeys(text);
    }

    /** Clicks the field to focus it; if something overlaps it, focuses it with JavaScript instead. */
    private void focus(WebElement el, By locator) {
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted on {} - focusing with JavaScript", locator);
            ((JavascriptExecutor) driver).executeScript(JS_FOCUS, el);
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected String value(By locator) {
        return visible(locator).getDomProperty(ATTR_VALUE);
    }

    protected void selectByVisibleText(By locator, String text) {
        new Select(visible(locator)).selectByVisibleText(text);
    }

    protected void selectByValue(By locator, String value) {
        new Select(visible(locator)).selectByValue(value);
    }

    protected String selectedOption(By locator) {
        return new Select(visible(locator)).getFirstSelectedOption().getText().trim();
    }

    // ---------- JavaScript ----------

    protected void scrollIntoView(WebElement el) {
        // re-applied every time: the page may have been reloaded / navigated since open()
        ((JavascriptExecutor) driver).executeScript(JS_DISABLE_SMOOTH_SCROLL + JS_SCROLL_INTO_VIEW, el);
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(JS_CLICK, el);
    }

    // ---------- alerts ----------

    protected Alert waitForAlert() {
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    // ---------- shadow DOM ----------

    /**
     * Finds an element inside (possibly nested) shadow roots.
     * Selenium only supports CSS selectors inside a shadow root.
     *
     * @param host      locator of the outermost shadow host (light DOM)
     * @param cssChain  CSS selectors: intermediate shadow hosts ..., target element last
     */
    protected WebElement shadowElement(By host, String... cssChain) {
        WebElement hostEl = visible(host);
        // the shadow root is attached by page JavaScript - wait until it exists
        SearchContext ctx = wait.until(d -> {
            try {
                return hostEl.getShadowRoot();
            } catch (NoSuchShadowRootException e) {
                return null;
            }
        });
        WebElement el = null;
        for (int i = 0; i < cssChain.length; i++) {
            el = ctx.findElement(By.cssSelector(cssChain[i]));
            if (i < cssChain.length - 1) {
                ctx = el.getShadowRoot();
            }
        }
        return el;
    }
}
