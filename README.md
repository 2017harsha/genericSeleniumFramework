# Generic Selenium Framework

A reusable UI test automation base that can be copied into any web project.

**Java 21 · Selenium 4.49 · TestNG 7.11 · Maven · Allure 2.29 · Jenkins · AWS Systems Manager Parameter Store**

The sample tests target <https://selectorshub.com/xpath-practice-page/>. They cover forms, XPath axes, dropdowns, alerts, a modal, shadow DOM, a dynamic data table and client-side form validation.

---

## Contents

1. [Features](#1-features)
2. [Prerequisites](#2-prerequisites)
3. [Project layout](#3-project-layout)
4. [Running the tests](#4-running-the-tests)
5. [Configuration and environments](#5-configuration-and-environments)
6. [Constants: `ConstantPages` and `ConstantTest`](#6-constants-constantpages-and-constanttest)
7. [Parallel, headless, cross-browser and Grid](#7-parallel-headless-cross-browser-and-grid)
8. [Retries](#8-retries)
9. [Failure evidence](#9-failure-evidence)
10. [Allure report](#10-allure-report)
11. [Secrets in AWS Parameter Store (prod)](#11-secrets-in-aws-parameter-store-prod)
12. [Jenkins pipeline](#12-jenkins-pipeline)
13. [Sample test cases](#13-sample-test-cases)
14. [Adding tests and reusing the framework in a new project](#14-adding-tests-and-reusing-the-framework-in-a-new-project)
15. [Built-in fixes for unreliable pages](#15-built-in-fixes-for-unreliable-pages)
16. [Troubleshooting](#16-troubleshooting)
17. [Change log](#17-change-log)

---

## 1. Features

| Area | What you get |
|---|---|
| Browsers | Chrome, Firefox and Edge, run locally or on a Selenium Grid. Selenium Manager downloads the drivers, so you don't install them. |
| Environments | `-Denv=qa \| dev \| prod` picks `config/<env>.properties`. Any value can be overridden with `-D` or an environment variable. |
| Secrets | A setting written as `ssm:/path` is fetched from AWS Parameter Store at run time. There are no passwords in the repo. |
| Parallel runs | Each test gets its own browser (stored per thread), so methods, classes, `<test>` blocks and data-provider rows can run in parallel. |
| Headless | On by default. Chrome and Edge use `--headless=new`; Firefox uses `-headless`. |
| Retries | A failing test runs up to **3 times** (the first run plus 2 retries) before it is marked failed. |
| Failure evidence | Each test that fails every attempt gets its own folder with a screenshot, page HTML, JSON details, stack trace and browser console log. The folder is wiped at the start of every run. |
| Reporting | Allure with steps, severity, epic and feature labels, environment info and failure attachments. |
| Constants | All locators, UI text, test data, groups and setting names live in `ConstantPages` / `ConstantTest`. |
| CI | A parameterized `Jenkinsfile` that works on Linux and Windows agents, with Allure, JUnit and archived artifacts. |

## 2. Prerequisites

| Tool | Version | Check |
|---|---|---|
| JDK | **21** or newer (`JAVA_HOME` must point to it) | `java -version` |
| Maven | 3.9 or newer | `mvn -version` |
| Browser | Chrome (Firefox and Edge are optional) | |
| IntelliJ IDEA (optional) | any recent version with the TestNG plugin | |
| AWS CLI (prod only) | v2 | `aws --version` |

Browser drivers are downloaded automatically. There's nothing else to install.

## 3. Project layout

```
generic-selenium-framework/
├── pom.xml                              dependencies, plugins and default run settings
├── Jenkinsfile                          parameterized CI pipeline
├── run-tests.bat                        one-click local run on Windows (runs the tests, then opens Allure)
├── docker-compose-grid.yml              optional local Selenium Grid (hub + Chrome + Firefox)
├── aws/
│   ├── setup-parameter-store.bat|.sh    stores the prod username and password in Parameter Store
│   └── jenkins-ssm-read-policy.json     read-only IAM policy for Jenkins
├── failure-evidence/                    created at run time (git-ignored), see section 9
│
├── src/main/java/com/qa/framework/      REUSABLE CORE: copy this unchanged to other projects
│   ├── base/
│   │   ├── BaseTest.java                opens a browser before each test and closes it after
│   │   ├── BasePage.java                waits, click/type with JS fallback, select, alerts, shadow DOM
│   │   ├── ConstantPages.java           ALL page constants: locators, UI text, JS, waits, browser args
│   │   └── ConstantTest.java            ALL test constants: groups, data providers, setting names, defaults, test data
│   ├── config/ConfigManager.java        environment settings, -D and env-var overrides, ssm: secrets
│   ├── driver/
│   │   ├── DriverFactory.java           Chrome/Firefox/Edge, headless, local or Grid, console-log capture
│   │   ├── DriverManager.java           one browser per thread
│   │   └── BrowserType.java
│   ├── listeners/
│   │   ├── ExecutionListener.java       wipes failure-evidence/ once at the start of every run
│   │   ├── SuiteConfigListener.java     applies -Dparallel / -Dthreads and writes Allure environment info
│   │   ├── TestListener.java            logging, per-attempt screenshots, evidence on the final failure
│   │   ├── RetryAnalyzer.java           up to retry.count retries; tracks attempts and errors
│   │   └── RetryTransformer.java        attaches RetryAnalyzer to every @Test automatically
│   ├── reporting/FailureEvidence.java   writes the per-test failure folders
│   └── secrets/AwsParameterStore.java   reads SecureString parameters from AWS (cached)
├── src/main/resources/
│   ├── log4j2.xml                       console log + target/logs/test-run.log
│   └── META-INF/services/org.testng.ITestNGListener   registers all listeners (no XML needed)
│
├── src/test/java/com/qa/                PROJECT-SPECIFIC: replace for your application
│   ├── pages/XPathPracticePage.java     page object for the sample site
│   └── tests/                           DummyFormTest, UserTableTest, AlertsAndModalTest,
│                                        ShadowDomTest, DataTableTest, PaymentFormTest
└── src/test/resources/
    ├── config/                          common.properties + qa / dev / prod .properties
    ├── suites/                          testng.xml (regression), smoke.xml, cross-browser.xml
    └── allure.properties
```

## 4. Running the tests

### 4.1 IntelliJ

First, once: choose **File → Reload All from Disk**, then click **Reload All Maven Projects** (⟳) in the Maven panel.

| To run | Do this |
|---|---|
| Full regression suite | `src/test/resources/suites/testng.xml`: right-click, **Run** |
| Smoke tests | `smoke.xml`: right-click, **Run** |
| Chrome, Firefox and Edge together | `cross-browser.xml`: right-click, **Run** |
| One class or method | Click the ▶ next to the class or method |

Add options under **Run → Edit Configurations → VM options**, for example `-Dheadless=false -Denv=dev -Dretry.count=0`.

### 4.2 Command Prompt

```bat
cd C:\path\to\generic-selenium-framework

mvn clean test                                                        :: qa, regression, chrome, headless, 3 attempts
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml     :: smoke tests only
mvn clean test -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml
mvn clean test -Denv=dev -Dbrowser=firefox -Dheadless=false           :: visible Firefox on dev
mvn clean test -Dtest=PaymentFormTest                                 :: one class
mvn clean test -Dtest=PaymentFormTest#shortCardNumberShowsError       :: one method
mvn clean test -Dparallel=classes -Dthreads=5                         :: override parallelism
mvn clean test -Dretry.count=0                                        :: no retries (debugging)
mvn clean test -Denv=prod                                             :: prod (needs AWS credentials)

run-tests.bat dev smoke.xml chrome false                              :: env suite browser headless [threads]
```

### 4.3 Jenkins

Open the job, click **Build with Parameters**, choose the settings and click **Build**. See section 12.

### 4.4 Where results go

| Output | Location |
|---|---|
| Allure report | `mvn allure:serve` locally, or **Allure Report** on the Jenkins build page |
| Failure evidence | `failure-evidence/` in the project root |
| Log file | console output and `target/logs/test-run.log` |
| TestNG/JUnit XML | `target/surefire-reports/` |

## 5. Configuration and environments

Settings are loaded in this order; later files override earlier ones:

1. `config/common.properties`: shared defaults
2. `config/<env>.properties`: `qa` (the default), `dev` or `prod`

Any key can then be overridden. Highest priority first:

1. JVM option: `-Dtimeout.explicit=30`
2. Environment variable: the key in upper case with `.` changed to `_`, e.g. `TIMEOUT_EXPLICIT=30`
3. The properties files above

| Key | Default | Meaning |
|---|---|---|
| `env` | `qa` | Environment to load |
| `app.url` | per env | Application URL |
| `app.username` / `app.password` | per env | Test credentials (prod uses `ssm:`) |
| `browser` | `chrome` | `chrome`, `firefox` or `edge` |
| `headless` | `true` | Run without a visible browser window |
| `gridUrl` | empty | Selenium Grid URL; empty means local browsers |
| `parallel` / `threads` | from the suite XML | Parallel mode and thread count |
| `retry.count` | `2` | Retries after the first run (2 means up to 3 attempts) |
| `timeout.explicit` | `15` | Seconds an explicit wait waits for an element |
| `timeout.pageLoad` / `timeout.script` | `60` / `30` | Seconds |
| `page.load.strategy` | `EAGER` | `NORMAL`, `EAGER` or `NONE`. EAGER stops waiting at DOMContentLoaded, which suits ad-heavy pages. |
| `window.size` | `1920,1080` | Browser window size |
| `failure.evidence.dir` | `failure-evidence` | Folder for failure evidence |
| `failure.evidence.clean` | `true` | Wipe the evidence folder at the start of each run |
| `aws.region` | `ap-south-1` | Region for `ssm:` lookups |

## 6. Constants: `ConstantPages` and `ConstantTest`

Every hard-coded value lives in one of two classes in `com.qa.framework.base`. Page and test classes only refer to constants.

| `ConstantPages` (the UI) | `ConstantTest` (the tests) |
|---|---|
| Locators (`EMAIL_INPUT`, `CARS_DROPDOWN`, …) and XPath templates | TestNG groups (`SMOKE`, `REGRESSION`) and data-provider names |
| Expected UI text (alert text, modal text, page title) | Setting names (`KEY_APP_URL`, `KEY_RETRY_COUNT`, …) and defaults |
| JavaScript snippets (scroll, click, focus, disable smooth scroll) | Test data (form rows, users, card numbers, search terms) |
| Default waits, window size, browser arguments | Failure-evidence file names and Allure attachment names |

Use them with a static import:

```java
import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;

@Test(groups = {SMOKE, REGRESSION})
public void selectCarFromDropdown() {
    XPathPracticePage page = new XPathPracticePage().open().selectCar(CAR_AUDI);
    assertEquals(page.selectedCar(), CAR_AUDI);
}
```

All the constants are compile-time constants, so they work inside annotations (`groups`, `dataProvider`, `@Parameters`).

## 7. Parallel, headless, cross-browser and Grid

- **Parallel:** each suite XML sets defaults, e.g. `testng.xml` uses `parallel="methods" thread-count="3"`. `-Dparallel=methods|classes|tests|none` and `-Dthreads=N` override them at run time; that's how Jenkins sets them. `-Dparallel=default` keeps the XML value.
- **Data providers:** `fillFormDataDriven` runs its rows in parallel (`parallel = true`).
- **Headless:** `-Dheadless=false` shows the browser.
- **Cross-browser:** `cross-browser.xml` runs the smoke tests on Chrome, Firefox and Edge at the same time, one `<test>` per browser with `parallel="tests"`.
- **Selenium Grid** (for example with Docker Desktop):
  ```bat
  docker compose -f docker-compose-grid.yml up -d
  mvn clean test -DgridUrl=http://localhost:4444 -Dthreads=4
  ```
  The Grid console is at <http://localhost:4444/ui>.

## 8. Retries

- By default a failing test runs **up to 3 times**: the first run plus `retry.count=2` retries.
- It is marked **FAILED only if all 3 attempts fail**. If a retry passes, the test is green, and the earlier attempts show as "retried" in TestNG and Allure.
- Each failed attempt is logged, e.g. `Attempt 1/3 of PaymentFormTest.shortCardNumberShowsError failed (…) - will retry`, and gets a screenshot in Allure.
- Data-provider rows are retried and counted separately.
- Skipped tests are not retried.
- To change it, use `-Dretry.count=0` (no retries), `retry.count` in `common.properties`, `DEFAULT_RETRY_COUNT` in `ConstantTest`, or the Jenkins `RETRY_COUNT` parameter.

## 9. Failure evidence

- **When it's written:** only for tests that failed **every** attempt. A test that passes on a retry leaves no folder.
- **Fresh folder per run:** `failure-evidence/` is **deleted and recreated once at the start of every run**, whether you start it from Maven, IntelliJ or Jenkins.

```
failure-evidence/
├── run-info.json                  environment, URL, browser, headless, grid, threads, Java, OS, start time
├── failures-summary.txt           one line per failed test, e.g.
│                                  001 | 2026-09-27T14:29:40 | PaymentFormTest.shortCardNumberShowsError | chrome | failed 3/3 attempts | …
└── 001_PaymentFormTest.shortCardNumberShowsError_chrome_142940-730/
    ├── screenshot.png             screen at the moment of the final failure
    ├── page-source.html           page HTML at that moment
    ├── failure-info.json          test, parameters, groups, env, browser + version, URL, title, thread, timings,
    │                              "attempts": 3 and "attemptErrors": the error of each attempt
    ├── stacktrace.txt
    └── browser-console.log        Chrome and Edge only (Firefox doesn't provide console logs)
```

- **Naming:** folders are numbered (`001_`, `002_` …) and include the browser and time, so parallel failures never overwrite each other.
- **Allure:** the same files are attached to the failed test in the report.
- **Jenkins:** archives `failure-evidence/**` with every build.
- **IntelliJ:** press **Ctrl+Alt+Y** (Reload from Disk) if the folder doesn't appear.

## 10. Allure report

Each run writes raw results to `target/allure-results/`. The report is built from them.

```bat
mvn allure:serve     :: builds the report and opens it in your browser (Ctrl+C to stop)
mvn allure:report    :: writes a copy to target/site/allure-maven-plugin/ (view it through a web server, not by double-clicking)
```

What you'll find in the report:

- **Overview:** totals, plus the run settings under **Environment**.
- **Suites** or **Behaviors:** every test, grouped by class or by epic/feature.
- **A failed test:** the steps (`@Step`), the attachments (screenshot, page source, failure info, browser console, URL), and a **Retries** tab with the earlier attempts.
- **Jenkins:** the **Allure Report** link on each build.

`mvn clean` deletes `target/`, including the results. IntelliJ doesn't clean between runs, so results from several runs pile up until you run `mvn clean` or delete `target/allure-results`.

## 11. Secrets in AWS Parameter Store (prod)

`prod.properties` holds references, not values:

```properties
app.username=ssm:/selenium-framework/prod/app.username
app.password=ssm:/selenium-framework/prod/app.password
```

Any value that starts with `ssm:` is fetched and decrypted when the tests run. It's cached for the rest of the run and never written to the log.

One-time setup:

1. Store the secrets as an AWS admin:
   ```bat
   aws\setup-parameter-store.bat <prod-username> <prod-password> ap-south-1
   ```
2. Give read access to whoever runs prod tests. Use `aws/jenkins-ssm-read-policy.json` and replace `<AWS_ACCOUNT_ID>`. Attach it to either:
   - the **IAM role** of the Jenkins agent, and leave the job's `AWS_CREDENTIALS_ID` parameter empty; or
   - an **IAM user** whose access key is saved in Jenkins as *AWS Credentials* with the ID `aws-selenium-prod`.
3. To run prod locally, set up credentials first (`aws configure`, or `set AWS_PROFILE=…`), then run `mvn clean test -Denv=prod`.

## 12. Jenkins pipeline

**One-time setup:**

1. **Plugins:** Pipeline, Git, **Allure Jenkins Plugin**, JUnit, Timestamper, **AWS Credentials**.
2. **Manage Jenkins → Tools:** a JDK named `JDK21`, Maven named `Maven3`, and Allure Commandline named `Allure` (install automatically).
3. **New Item → Pipeline →** "Pipeline script from SCM", pointing at your Git repo, with Script Path `Jenkinsfile`.
4. Click **Build Now** once so Jenkins picks up the parameters. After that, use **Build with Parameters**.

| Parameter | Values / default |
|---|---|
| `ENV` | `qa` / `dev` / `prod` |
| `SUITE` | `testng.xml` / `smoke.xml` / `cross-browser.xml` |
| `BROWSER` | `chrome` / `firefox` / `edge` |
| `HEADLESS` | `true` |
| `PARALLEL` | `default` (use the suite XML) / `methods` / `classes` / `tests` / `none` |
| `THREADS` | `3` |
| `RETRY_COUNT` | `2` (up to 3 attempts) |
| `GRID_URL` | empty means local browsers |
| `AWS_CREDENTIALS_ID` / `AWS_REGION` | used only for `prod` |

After a build you get:
- the build marked **UNSTABLE** (yellow) if any test failed
- an **Allure Report** link
- test results via JUnit
- archived `failure-evidence/**`, logs and surefire reports

The pipeline runs on Linux (`sh`) and Windows (`bat`) agents.

## 13. Sample test cases

The suite has 19 test methods, which make 23 runs once data-provider rows are counted. The 5 marked ✔ in the Smoke column are the smoke set.

| Class | Test | Smoke | What it checks |
|---|---|---|---|
| DummyFormTest | fillFormWithEnvironmentCredentials | ✔ | Fills the form with the environment's credentials (prod ones come from SSM); password field is masked |
| | fillFormDataDriven (3 rows, parallel) | | Data-driven form fill and submit |
| | typeInFirstCrushField | | Text input and page title |
| UserTableTest | tableHasSixUsers | ✔ | Row count |
| | selectUserCheckboxUsingXpathAxes | | Ticks a row's checkbox found with XPath `preceding-sibling` |
| | verifyUserDetails (3 rows) | | Role, name and status found with XPath `following-sibling` |
| AlertsAndModalTest | acceptConfirmAlert | ✔ | Confirm alert text and accept |
| | dismissConfirmAlert | | Confirm alert dismiss |
| | answerPromptAlert | | Prompt alert: types an answer and accepts |
| | openAndCloseModal | | Modal content and closing it |
| | selectCarFromDropdown | ✔ | `<select>` dropdown |
| ShadowDomTest | typeInsideShadowRoot | ✔ | Input inside a shadow root |
| | typeInsideNestedShadowRoot | | Input inside a nested shadow root |
| DataTableTest | searchFiltersRows | | Search filters the rows and the info text says "filtered" |
| | changePageLength | | Choosing 25 rows per page shows 25 rows |
| PaymentFormTest | cardNumberIsAutoFormatted | | 16 digits are formatted as `4111 1111 1111 1111` |
| | shortCardNumberShowsError | | Invalid card number shows the error and adds the `invalid` class |
| | expiryIsAutoFormatted | | `1229` becomes `12/29` |
| | invalidNameIsFlagged | | Digits in the name are rejected |

The payment form is never submitted. The tests use a well-known dummy test card number.

## 14. Adding tests and reusing the framework in a new project

1. Copy the project, or publish the core with `mvn install` and add it as a dependency.
2. Add locators and UI text to **`ConstantPages`**, and test data, groups and data-provider names to **`ConstantTest`**.
3. Create a page class under `src/test/java/.../pages` that `extends BasePage`. Use its helpers (`visible`, `click`, `type`, `selectByVisibleText`, `waitForAlert`, `shadowElement`, …) and label methods with `@Step`.
4. Create a test class under `src/test/java/.../tests` that `extends BaseTest`, then add `@Test(groups = …)`, `@Epic` / `@Feature` / `@Severity`.
5. Point `app.url` (and the credentials) in `config/*.properties` at your application, and update the SSM paths in `prod.properties`.
6. Add the new package or classes to the suite XMLs if you use a different package name.

## 15. Built-in fixes for unreliable pages

These fixes came from problems found on the sample site. They apply to every page:

- **IDs that change every load:** the sample page renames the email field to `shub<random 1-100>` on each load, so its locator uses `input[name='email']`. Don't locate elements by IDs like that.
- **Smooth scrolling:** sites with CSS `scroll-behavior: smooth` make Selenium click while the page is still scrolling, which causes `ElementClickInterceptedException`. `BasePage` turns smooth scrolling off after every `open()` and before every scroll, and always scrolls instantly to the centre of the screen.
- **Blocked clicks:** `click()` falls back to a JavaScript click, and `type()` falls back to focusing the field with JavaScript, if something is covering the element.
- **Content added by scripts:** the data table and shadow-DOM fields are created by page scripts that can finish after `driver.get()` returns. The page class waits for the table's info text, and `shadowElement()` waits for the shadow root to exist.
- **Explicit waits only:** the implicit wait is 0, and every lookup goes through `WebDriverWait`.

## 16. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| IntelliJ still shows old code or an old version in `pom.xml` | A file changed on disk while it was open. Choose **File → Reload All from Disk**, then reload the Maven projects. Don't save an old editor tab over a newer file. |
| `failure-evidence` folder isn't visible in IntelliJ | Press **Ctrl+Alt+Y** (Reload from Disk). The folder is in the project root, not in `target/`. |
| `Unable to find CDP implementation matching <version>` warning | Chrome is newer than the Selenium version. This is harmless, because the framework doesn't use those developer tools. Raise `selenium.version` in `pom.xml` to remove the warning. |
| `ElementClickInterceptedException` | Should be handled by the smooth-scroll and JavaScript fallbacks. If it still happens, open the evidence screenshot to see what's covering the element. |
| `TimeoutException` waiting for an element | Check the locator against the live page, and whether its ID changes between loads. Run with `-Dheadless=false -Dretry.count=0` to watch. |
| The Allure `index.html` is blank when opened directly | Browsers block the report's scripts when a file is opened from disk. Use `mvn allure:serve`. |
| No Allure results | `mvn clean` deletes `target/allure-results`. Generate the report before cleaning. |
| `Could not read SSM parameter …` on prod | Check your AWS credentials, the region, and the `ssm:GetParameter` + `kms:Decrypt` permissions. |
| `Missing required config 'app.url' …` | The key isn't set for that env. Add it to `config/<env>.properties` or pass it with `-D`. |

## 17. Change log

| Change | Details |
|---|---|
| Initial framework | Java, Selenium, TestNG, Maven, Allure, parameterized Jenkins pipeline, parallel and headless runs, AWS Parameter Store for prod secrets, 23 sample test runs |
| Java 21 | `java.version=21`, Jenkins tool `JDK21` |
| Constants | All values moved to `ConstantPages` / `ConstantTest` and used through static imports |
| Selenium 4.49.0 | Supports newer Chrome versions (was 4.35.0) |
| Email field locator | Now `input[name='email']`, because the page changes the field's ID on every load |
| Failure evidence | One folder per failed test (PNG, HTML, JSON, stack trace, console log), wiped at the start of every run, archived in Jenkins |
| Page reliability | Smooth scrolling turned off, instant scroll, JavaScript focus/click fallbacks, waits for the data table and shadow DOM |
| Retries | 1 run + 2 retries (`retry.count=2`, Jenkins `RETRY_COUNT`). Evidence is written only when all 3 attempts fail, and `failure-info.json` records each attempt's error. |
