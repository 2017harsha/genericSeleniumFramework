package com.qa.framework.base;

/**
 * Single place for every test-level constant: TestNG groups, data-provider names, configuration
 * keys and defaults, report settings, secrets settings and test data.
 *
 * <p>Test and framework classes use these via {@code import static com.qa.framework.base.ConstantTest.*;}.
 * All values are compile-time constants, so they can be used inside annotations
 * (e.g. {@code @Test(groups = SMOKE)}).
 */
public final class ConstantTest {

    private ConstantTest() {
        // constants holder - no instances
    }

    // =====================================================================================
    // TestNG groups & data providers
    // =====================================================================================
    public static final String SMOKE = "smoke";
    public static final String REGRESSION = "regression";

    public static final String DP_FORM_DATA = "formData";
    public static final String DP_USERS = "users";

    // =====================================================================================
    // Configuration keys (config/*.properties, -Dkey=value, or env var KEY_NAME)
    // =====================================================================================
    public static final String KEY_ENV = "env";
    public static final String KEY_ENV_VAR = "ENV";
    public static final String KEY_APP_URL = "app.url";
    public static final String KEY_APP_USERNAME = "app.username";
    public static final String KEY_APP_PASSWORD = "app.password";
    public static final String KEY_BROWSER = "browser";
    public static final String KEY_HEADLESS = "headless";
    public static final String KEY_GRID_URL = "gridUrl";
    public static final String KEY_PARALLEL = "parallel";
    public static final String KEY_THREADS = "threads";
    public static final String KEY_RETRY_COUNT = "retry.count";
    public static final String KEY_TIMEOUT_EXPLICIT = "timeout.explicit";
    public static final String KEY_TIMEOUT_PAGE_LOAD = "timeout.pageLoad";
    public static final String KEY_TIMEOUT_SCRIPT = "timeout.script";
    public static final String KEY_PAGE_LOAD_STRATEGY = "page.load.strategy";
    public static final String KEY_WINDOW_SIZE = "window.size";
    public static final String KEY_AWS_REGION = "aws.region";
    public static final String KEY_AWS_REGION_ENV_VAR = "AWS_REGION";
    public static final String KEY_ALLURE_RESULTS_DIR = "allure.results.directory";

    // TestNG suite XML parameter (cross-browser suite)
    public static final String PARAM_BROWSER = "browser";

    // =====================================================================================
    // Defaults
    // =====================================================================================
    public static final String DEFAULT_ENV = "qa";
    public static final String DEFAULT_BROWSER = "chrome";
    public static final boolean DEFAULT_HEADLESS = true;
    /** Extra attempts after the first run: 2 -> a test runs up to 3 times before it is reported as failed. */
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final String DEFAULT_AWS_REGION = "ap-south-1";
    public static final String DEFAULT_ALLURE_RESULTS_DIR = "target/allure-results";
    /** -Dparallel / -Dthreads value meaning "keep what the suite XML says". */
    public static final String SUITE_DEFAULT = "default";

    // =====================================================================================
    // Config files & secrets
    // =====================================================================================
    public static final String CONFIG_DIR = "config/";
    public static final String COMMON_CONFIG_FILE = CONFIG_DIR + "common.properties";
    public static final String PROPERTIES_EXT = ".properties";
    public static final String SSM_PREFIX = "ssm:";

    // =====================================================================================
    // Allure
    // =====================================================================================
    public static final String ALLURE_ENV_FILE = "environment.properties";
    public static final String ATTACH_SCREENSHOT = "Screenshot on failure";
    public static final String ATTACH_URL = "URL";
    public static final String ATTACH_PAGE_SOURCE = "Page source";
    public static final String MIME_PNG = "image/png";
    public static final String MIME_HTML = "text/html";
    public static final String EXT_PNG = "png";
    public static final String EXT_HTML = "html";

    // =====================================================================================
    // Failure evidence (one folder per failed test, wiped at the start of every run)
    // =====================================================================================
    public static final String KEY_FAILURE_DIR = "failure.evidence.dir";
    public static final String KEY_FAILURE_CLEAN = "failure.evidence.clean";
    public static final String DEFAULT_FAILURE_DIR = "failure-evidence";
    public static final boolean DEFAULT_FAILURE_CLEAN = true;

    public static final String FAILURE_SCREENSHOT_FILE = "screenshot.png";
    public static final String FAILURE_PAGE_SOURCE_FILE = "page-source.html";
    public static final String FAILURE_INFO_FILE = "failure-info.json";
    public static final String FAILURE_STACKTRACE_FILE = "stacktrace.txt";
    public static final String FAILURE_CONSOLE_LOG_FILE = "browser-console.log";
    public static final String RUN_INFO_FILE = "run-info.json";
    public static final String FAILURE_SUMMARY_FILE = "failures-summary.txt";

    /** Folder name: 001_DummyFormTest.fillFormDataDriven_chrome_134402-517 */
    public static final String FAILURE_FOLDER_FORMAT = "%03d_%s.%s_%s_%s";
    public static final String FAILURE_TIME_PATTERN = "HHmmss-SSS";
    public static final String ISO_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String ATTACH_FAILURE_INFO = "Failure info";
    public static final String ATTACH_CONSOLE_LOG = "Browser console";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_TEXT = "text/plain";
    public static final String EXT_JSON = "json";
    public static final String EXT_TXT = "txt";

    // Capability names that turn on browser console-log capture
    public static final String CAP_CHROME_LOGGING_PREFS = "goog:loggingPrefs";
    public static final String CAP_EDGE_LOGGING_PREFS = "ms:loggingPrefs";

    // =====================================================================================
    // Test data - dummy form
    // =====================================================================================
    public static final String COMPANY_NAME = "Acme Corp";
    public static final String MOBILE_NUMBER = "9876543210";
    public static final String FIRST_CRUSH_NAME = "Selenium";

    /** {email, password, company, mobile} - rows run in parallel. */
    public static final Object[][] FORM_DATA = {
            {"alice@example.com", "Alice@123", "SelectorsHub", "9000000001"},
            {"bob@example.com", "Bob@12345", "TestCo", "9000000002"},
            {"carol@example.com", "Carol@123", "QA Labs", "9000000003"},
    };

    // =====================================================================================
    // Test data - user table
    // =====================================================================================
    public static final int EXPECTED_USER_COUNT = 6;
    public static final String USER_JOHN_SMITH = "John.Smith";
    public static final String USER_JOE_ROOT = "Joe.Root";

    /** {username, role, employee name} */
    public static final Object[][] USERS = {
            {"John.Smith", "Admin", "John Smith"},
            {"Joe.Root", "ESS", "Joe Root"},
            {"Kevin.Mathews", "ESS", "Kevin Mathews"},
    };

    // =====================================================================================
    // Test data - dropdown, prompt, shadow DOM
    // =====================================================================================
    public static final String CAR_AUDI = "Audi";
    public static final String PROMPT_ANSWER = "Yes";
    public static final String SHADOW_USERNAME = "Harsha";
    public static final String SHADOW_PIZZA = "Margherita";

    // =====================================================================================
    // Test data - data table
    // =====================================================================================
    public static final String DATATABLE_SEARCH_TERM = "India";
    public static final int DATATABLE_DEFAULT_PAGE_SIZE = 10;
    public static final String DATATABLE_PAGE_SIZE_25 = "25";
    public static final int DATATABLE_EXPECTED_ROWS_25 = 25;

    // =====================================================================================
    // Test data - payment form (dummy test card; the form is never submitted)
    // =====================================================================================
    public static final String TEST_CARD_NUMBER = "4111111111111111";
    public static final String TEST_CARD_NUMBER_FORMATTED = "4111 1111 1111 1111";
    public static final String SHORT_CARD_NUMBER = "1234";
    public static final String EXPIRY_RAW = "1229";
    public static final String EXPIRY_FORMATTED = "12/29";
    public static final String INVALID_CARD_NAME = "J1";
}
