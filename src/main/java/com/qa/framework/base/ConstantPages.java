package com.qa.framework.base;

/**
 * Single place for every page-level constant: waits, browser/window settings, JavaScript snippets,
 * DOM attribute names, locators and expected UI text.
 *
 * <p>Locators are String constants used by PageFactory {@code @FindBy} annotations in the page classes.
 *
 * <p>Page classes use these via {@code import static com.qa.framework.base.ConstantPages.*;}.
 * When the UI changes, update the locator here only.
 */
public final class ConstantPages {

    private ConstantPages() {
        // constants holder - no instances
    }

    // =====================================================================================
    // Waits & browser (defaults; each can be overridden from config / -D)
    // =====================================================================================
    public static final int DEFAULT_EXPLICIT_WAIT_SEC = 15;
    public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SEC = 60;
    public static final int DEFAULT_SCRIPT_TIMEOUT_SEC = 30;
    public static final String DEFAULT_WINDOW_SIZE = "1920,1080";
    public static final String DEFAULT_PAGE_LOAD_STRATEGY = "NORMAL";

    // Browser command-line arguments
    public static final String ARG_WINDOW_SIZE = "--window-size=";
    public static final String ARG_DISABLE_NOTIFICATIONS = "--disable-notifications";
    public static final String ARG_DISABLE_SEARCH_ENGINE_CHOICE = "--disable-search-engine-choice-screen";
    public static final String ARG_NO_SANDBOX = "--no-sandbox";
    public static final String ARG_DISABLE_DEV_SHM = "--disable-dev-shm-usage";
    public static final String ARG_CHROMIUM_HEADLESS = "--headless=new";
    public static final String ARG_DISABLE_GPU = "--disable-gpu";
    public static final String ARG_FIREFOX_HEADLESS = "-headless";
    public static final String ARG_FIREFOX_WIDTH = "--width=";
    public static final String ARG_FIREFOX_HEIGHT = "--height=";

    // =====================================================================================
    // JavaScript snippets
    // =====================================================================================
    // behavior:'instant' overrides the site's CSS smooth scrolling, so the element has really
    // arrived in the viewport before Selenium clicks it (avoids ElementClickInterceptedException)
    public static final String JS_SCROLL_INTO_VIEW = "arguments[0].scrollIntoView({block:'center', inline:'nearest', behavior:'instant'});";
    public static final String JS_FOCUS = "arguments[0].focus();";
    // Many sites set CSS "scroll-behavior: smooth". Selenium's own click() then scrolls with an
    // animation and clicks while the element is still moving -> ElementClickInterceptedException.
    // Forcing "auto" makes every scroll (ours and Selenium's) instant.
    public static final String JS_DISABLE_SMOOTH_SCROLL =
            "document.documentElement.style.setProperty('scroll-behavior','auto','important');"
            + "if (document.body) { document.body.style.setProperty('scroll-behavior','auto','important'); }";
    public static final String JS_CLICK = "arguments[0].click();";

    // =====================================================================================
    // DOM attribute / property names
    // =====================================================================================
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CLASS = "class";
    public static final String CSS_CLASS_INVALID = "invalid";
    public static final String WHITESPACE_REGEX = "\\s+";

    // =====================================================================================
    // XPath Practice Page - expected UI text
    // =====================================================================================
    public static final String PRACTICE_PAGE_TITLE = "Xpath Practice Page";
    public static final String CONFIRM_ALERT_TEXT = "Press a button!";
    public static final String PROMPT_ALERT_TEXT = "Do you have Testing Daily Mobile App?";
    public static final String MODAL_BODY_TEXT = "Testing Daily";
    public static final String USER_STATUS_ENABLED = "Enabled";
    public static final String PASSWORD_INPUT_TYPE = "password";
    public static final String DATATABLE_FILTERED_TEXT = "filtered";
    public static final String DATATABLE_INFO_PREFIX = "Showing 1 to ";

    // Payment form field ids (used for "is marked invalid" checks)
    public static final String ID_CARD_NAME = "cardName";
    public static final String ID_CARD_NUMBER = "cardNumber";
    public static final String ID_EXPIRY = "expiry";
    public static final String ID_CVV = "cvv";

    // User-table column offsets after the Username column
    public static final int COL_USER_ROLE = 1;
    public static final int COL_EMPLOYEE_NAME = 2;
    public static final int COL_STATUS = 3;

    // =====================================================================================
    // XPath Practice Page - locators
    // =====================================================================================
    // Every locator is a String compile-time constant so it can be used in PageFactory annotations,
    // e.g. @FindBy(css = EMAIL_INPUT_CSS). Locators that must be built at runtime are String
    // templates (…_XPATH with %s / %d) turned into a By inside the page class.

    // Dummy form
    // NOTE: the page renames this input's id to "shub" + random(1..100) on every load
    // (practice scenario for dynamic ids), so never locate it by id - name is stable.
    public static final String EMAIL_INPUT_CSS = "input[name='email']";
    public static final String PASSWORD_INPUT_ID = "pass";
    public static final String COMPANY_INPUT_CSS = "input[name='company']";          // first match = visible one
    public static final String MOBILE_INPUT_CSS = "input[name='mobile number']";
    public static final String SUBMIT_BUTTON_XPATH = "//button[@value='Submit']";
    public static final String FIRST_CRUSH_INPUT_ID = "inp_val";

    // User table (XPath axes). Row list is a @FindBy; the per-user locators are templates
    // (%s = username, %d = column offset) formatted at runtime into a By by the page class.
    public static final String USER_TABLE_ROWS_XPATH = "//table[@id='resultTable']//tr[td]";
    public static final String USER_CHECKBOX_XPATH =
            "//table[@id='resultTable']//a[normalize-space()='%s']/parent::td/preceding-sibling::td/input[@type='checkbox']";
    public static final String USER_CELL_XPATH =
            "//table[@id='resultTable']//a[normalize-space()='%s']/parent::td/following-sibling::td[%d]";

    // Dropdown
    public static final String CARS_DROPDOWN_ID = "cars";

    // Alerts & modal
    public static final String WINDOW_ALERT_BUTTON_XPATH = "//button[normalize-space()='Click To Open Window Alert']";
    public static final String PROMPT_ALERT_BUTTON_XPATH = "//button[normalize-space()='Click To Open Window Prompt Alert']";
    public static final String OPEN_MODAL_BUTTON_ID = "myBtn";
    public static final String MODAL_ID = "myModal";
    public static final String MODAL_BODY_CSS = "#myModal .modal-body";
    public static final String MODAL_CLOSE_CSS = "#myModal .close";

    // Shadow DOM: the host is a normal element (@FindBy works); elements INSIDE shadow roots cannot
    // be reached by @FindBy - they are CSS selectors resolved through getShadowRoot().
    public static final String SHADOW_HOST_ID = "userName";
    public static final String SHADOW_USERNAME_CSS = "#kils";
    public static final String SHADOW_NESTED_HOST_CSS = "#app2";
    public static final String SHADOW_PIZZA_CSS = "#pizza";

    // DataTable (tablepress)
    public static final String DATATABLE_SEARCH_ID = "dt-search-0";
    public static final String DATATABLE_ROWS_CSS = "#tablepress-1 tbody tr";
    public static final String DATATABLE_INFO_ID = "tablepress-1_info";
    public static final String DATATABLE_PAGE_LENGTH_ID = "dt-length-0";

    // Payment form (ID_CARD_NAME, ID_CARD_NUMBER, ID_EXPIRY, ID_CVV are defined above)
    public static final String CARD_NUMBER_ERROR_ID = "cardNumberError";

}
