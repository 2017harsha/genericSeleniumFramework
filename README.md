# Generic Selenium Framework

Reusable UI automation base: **Java 21 · Selenium 4 · TestNG · Maven · Allure · Jenkins · AWS Parameter Store**.
Sample tests target <https://selectorshub.com/xpath-practice-page/>.

## Project layout

```
src/main/java/com/qa/framework      <- reusable core (copy / publish as-is to other projects)
  base/BaseTest.java                   browser per test method (ThreadLocal) -> parallel-safe
  base/BasePage.java                   waits, click w/ JS fallback, type, select, alerts, shadow DOM
  base/ConstantPages.java              ALL page constants: locators, UI text, JS, waits, browser args
  base/ConstantTest.java               ALL test constants: groups, data providers, config keys, defaults, test data
  config/ConfigManager.java            env config + -D / ENV-VAR overrides + "ssm:" secret resolution
  driver/DriverFactory.java            chrome / firefox / edge, headless, local or Selenium Grid
  driver/DriverManager.java            ThreadLocal<WebDriver>
  secrets/AwsParameterStore.java       reads SecureString params from AWS SSM (cached)
  listeners/SuiteConfigListener.java   -Dparallel / -Dthreads override + Allure environment
  listeners/TestListener.java          logs + per-test failure evidence folder + Allure attachments
  listeners/ExecutionListener.java     wipes failure-evidence/ once at the start of every run
  reporting/FailureEvidence.java       writes screenshot, page source, JSON, stack trace, console log
  listeners/RetryAnalyzer|Transformer  auto-retry failed tests (retry.count)
src/main/resources/META-INF/services   auto-registers the listeners (no XML needed)

src/test/java/com/qa/pages          <- project-specific page objects
src/test/java/com/qa/tests          <- project-specific tests
src/test/resources/config           common / qa / dev / prod properties
src/test/resources/suites           testng.xml (regression), smoke.xml, cross-browser.xml
Jenkinsfile                          parameterized pipeline
aws/                                 Parameter Store setup scripts + IAM policy
docker-compose-grid.yml              optional local Selenium Grid
run-tests.bat                        one-click local run on Windows
```

## Run locally (Windows CMD)

Prerequisites: JDK 21+, Maven 3.9+, Chrome (Firefox/Edge optional). Driver binaries are downloaded automatically by Selenium Manager.

```bat
mvn clean test                                   :: qa, regression suite, chrome, headless
mvn clean test -Denv=dev -Dheadless=false        :: watch the browser
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml -Dbrowser=edge
mvn clean test -Dparallel=classes -Dthreads=5    :: override parallelism
mvn clean test -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml
mvn allure:serve                                 :: open the Allure report
run-tests.bat dev smoke.xml firefox false        :: helper script
```

Any config key can be overridden: `-Dtimeout.explicit=30`, or an env var `TIMEOUT_EXPLICIT=30`.

## Parallel + headless

* `BaseTest` creates a new driver per test method and stores it in a `ThreadLocal`, so methods, classes, tests and data-provider rows can all run concurrently.
* Defaults live in the suite XML (`parallel="methods" thread-count="3"`); `-Dparallel` / `-Dthreads` override them at runtime (used by Jenkins).
* `-Dheadless=true` (default) uses `--headless=new` for Chrome/Edge and `-headless` for Firefox with a fixed 1920x1080 window.
* For bigger runs point at a Grid: `docker compose -f docker-compose-grid.yml up -d` then `-DgridUrl=http://localhost:4444`.

## Failure evidence (one folder per failed test)

Every run starts by **deleting** the previous `failure-evidence/` folder and creating a fresh one.

**Retries:** a failing test is re-run up to `retry.count` more times (default **2**, i.e. up to 3 attempts).
It is reported as FAILED - and gets an evidence folder - only if **all** attempts fail. If a retry passes,
the test is green and no folder is written (earlier attempts show as "retried" in TestNG/Allure, each with a screenshot).
Override with `-Dretry.count=0` (no retries) or the Jenkins `RETRY_COUNT` parameter.

Each test that failed on every attempt gets its own numbered folder:

```
failure-evidence/
├── run-info.json                 env, browser, headless, grid, threads, start time
├── failures-summary.txt          one line per failed test ("failed 3/3 attempts") -> folder name
├── 001_DummyFormTest.fillFormWithEnvironmentCredentials_chrome_134402-517/
│   ├── screenshot.png
│   ├── page-source.html          DOM at the moment of failure
│   ├── failure-info.json         test, parameters, groups, env, browser + version, URL, title, timings,
│   │                             attempts (3) and the error of every attempt
│   ├── stacktrace.txt
│   └── browser-console.log       Chrome / Edge (Firefox does not expose console logs)
└── 002_...
```

The same files are attached to the failed test in the Allure report, and Jenkins archives the folder with each build.
Settings (in `ConstantTest` / config): `failure.evidence.dir` (default `failure-evidence`),
`failure.evidence.clean=false` to keep old folders.

## Environments & secrets

`-Denv=qa|dev|prod` loads `config/common.properties` then `config/<env>.properties`.

Prod secrets are **not** in the repo:

```properties
app.password=ssm:/selenium-framework/prod/app.password
```

Any value starting with `ssm:` is fetched and decrypted from AWS Systems Manager Parameter Store at runtime (cached, never logged).

One-time AWS setup:

1. Create the parameters: `aws\setup-parameter-store.bat <user> <password> ap-south-1`
2. Give Jenkins read access: attach `aws/jenkins-ssm-read-policy.json` (replace `<AWS_ACCOUNT_ID>`) to either
   * the IAM **role** of the Jenkins agent (EC2/ECS/EKS) - leave `AWS_CREDENTIALS_ID` empty in the job, or
   * an IAM **user** whose access key is saved in Jenkins as *AWS Credentials* with id `aws-selenium-prod`.
3. Running prod locally: `aws configure` (or `set AWS_PROFILE=...`) then `mvn test -Denv=prod`.

## Jenkins job

1. Plugins: Pipeline, Git, **Allure Jenkins Plugin**, JUnit, Timestamper, **AWS Credentials**.
2. *Manage Jenkins -> Tools*: JDK named `JDK21`, Maven named `Maven3`, Allure Commandline named `Allure` (install automatically).
3. New Item -> **Pipeline** -> "Pipeline script from SCM" -> your Git repo -> Script Path `Jenkinsfile`.
4. Run once ("Build Now") so Jenkins registers the parameters; afterwards use **Build with Parameters**:

| Parameter | Values |
|---|---|
| ENV | qa / dev / prod |
| SUITE | testng.xml / smoke.xml / cross-browser.xml |
| BROWSER | chrome / firefox / edge |
| HEADLESS | true / false |
| PARALLEL | default / methods / classes / tests / none |
| THREADS | e.g. 3 |
| RETRY_COUNT | retries after the first run (default 2 = 3 attempts) |
| GRID_URL | optional Selenium Grid |
| AWS_CREDENTIALS_ID / AWS_REGION | used only for prod |

The build turns **UNSTABLE** (yellow) on test failures and the Allure report link appears on the build page.

## Reusing for a new project

1. Copy the repo (or publish `src/main` as a jar with `mvn install` and depend on it).
2. Replace `com.qa.pages` / `com.qa.tests` with your pages and tests - pages `extend BasePage`, tests `extend BaseTest`.
   Put new locators/UI text in `ConstantPages` and new test data/groups in `ConstantTest`; never hard-code them in
   page or test classes (`import static com.qa.framework.base.ConstantPages.*;`).
3. Update `app.url` etc. in `config/*.properties` and the SSM paths in `prod.properties`.
