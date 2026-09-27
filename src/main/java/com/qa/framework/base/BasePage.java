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
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;

/**
 * Parent of every page object.
 *
 * <p><b>PageFactory:</b> the constructor calls {@link PageFactory#initElements} with an
 * {@link AjaxElementLocatorFactory}, so every {@code @FindBy} field in a subclass is a lazy proxy that
 * (a) looks the element up again on EVERY use - no stale references after the page re-renders - and
 * (b) waits up to {@code timeout.explicit} seconds for it to appear. Do NOT use {@code @CacheLookup} on
 * elements of dynamic pages.
 *
 * <p>Helpers take the page's {@code @FindBy} {@code WebElement} fields. A small set of {@code By}
 * overloads (visible, clickable, click, text) exists only for locators built at runtime from
 * templates. All helpers wait, scroll instantly and fall back to JavaScript when a click is intercepted.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    @SuppressWarnings("this-escape") // intentional: PageFactory must populate the subclass' @FindBy fields
    protected BasePage() {
        this.driver = DriverManager.getDriver();
        int timeout = ConfigManager.getInt(KEY_TIMEOUT_EXPLICIT, DEFAULT_EXPLICIT_WAIT_SEC);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        this.wait.ignoring(StaleElementReferenceException.class);
        // Initialise all @FindBy / @FindBys / @FindAll fields of the concrete page class.
        // (Subclass fields have no initialiser, so the proxies set here are kept.)
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, timeout), this);
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

    // =====================================================================================
    // WebElement (PageFactory) helpers
    // =====================================================================================

    protected WebElement visible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement clickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected List<WebElement> allVisible(List<WebElement> elements) {
        return wait.until(ExpectedConditions.visibilityOfAllElements(elements));
    }

    /** Note: with AjaxElementLocatorFactory a MISSING element is waited for (timeout) before returning false. */
    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean waitForInvisibility(WebElement element) {
        try {
            return wait.until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void click(WebElement element) {
        WebElement el = clickable(element);
        scrollIntoView(el);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted on {} - retrying with JavaScript", describe(element));
            jsClick(el);
        }
    }

    protected void type(WebElement element, String text) {
        WebElement el = visible(element);
        scrollIntoView(el);
        focus(el);   // some fields (e.g. readonly-until-focus) need focus first
        el.clear();
        el.sendKeys(text);
    }

    protected String text(WebElement element) {
        return visible(element).getText().trim();
    }

    protected String value(WebElement element) {
        return visible(element).getDomProperty(ATTR_VALUE);
    }

    protected void selectByVisibleText(WebElement element, String text) {
        new Select(visible(element)).selectByVisibleText(text);
    }

    protected void selectByValue(WebElement element, String value) {
        new Select(visible(element)).selectByValue(value);
    }

    protected String selectedOption(WebElement element) {
        return new Select(visible(element)).getFirstSelectedOption().getText().trim();
    }

    /** Waits until the element's text is different from {@code previousText} (e.g. after filtering). */
    protected void waitForTextChange(WebElement element, String previousText) {
        wait.until(d -> !element.getText().trim().equals(previousText));
    }

    // =====================================================================================
    // By helpers - only for locators built at RUNTIME (e.g. from an XPath template + a username),
    // which cannot be expressed in a @FindBy annotation.
    // =====================================================================================

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        click(clickable(locator));
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    // =====================================================================================
    // JavaScript, focus, alerts
    // =====================================================================================

    /** Clicks the field to focus it; if something overlaps it, focuses it with JavaScript instead. */
    private void focus(WebElement el) {
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted on {} - focusing with JavaScript", describe(el));
            ((JavascriptExecutor) driver).executeScript(JS_FOCUS, el);
        }
    }

    protected void scrollIntoView(WebElement el) {
        // re-applied every time: the page may have been reloaded / navigated since open()
        ((JavascriptExecutor) driver).executeScript(JS_DISABLE_SMOOTH_SCROLL + JS_SCROLL_INTO_VIEW, el);
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(JS_CLICK, el);
    }

    protected Alert waitForAlert() {
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    /** PageFactory proxies print their locator in toString(), which makes log lines readable. */
    private static String describe(WebElement el) {
        try {
            return String.valueOf(el);
        } catch (RuntimeException e) {
            return "element";
        }
    }

    // =====================================================================================
    // Shadow DOM (elements inside a shadow root cannot be @FindBy fields)
    // =====================================================================================

    /**
     * Finds an element inside (possibly nested) shadow roots. Selenium only supports CSS selectors
     * inside a shadow root.
     *
     * @param host      the outermost shadow host (a normal element - usually a @FindBy field)
     * @param cssChain  CSS selectors: intermediate shadow hosts ..., target element last
     */
    protected WebElement shadowElement(WebElement host, String... cssChain) {
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
