@echo off
REM Quick local runner for Windows CMD.
REM   run-tests.bat                      -> qa, regression suite, chrome, headless
REM   run-tests.bat dev smoke.xml firefox false
REM Args: [env] [suite] [browser] [headless] [threads]
setlocal
set ENV=%~1
if "%ENV%"=="" set ENV=qa
set SUITE=%~2
if "%SUITE%"=="" set SUITE=testng.xml
set BROWSER=%~3
if "%BROWSER%"=="" set BROWSER=chrome
set HEADLESS=%~4
if "%HEADLESS%"=="" set HEADLESS=true
set THREADS=%~5
if "%THREADS%"=="" set THREADS=3

call mvn clean test -Denv=%ENV% -DsuiteXmlFile=src/test/resources/suites/%SUITE% -Dbrowser=%BROWSER% -Dheadless=%HEADLESS% -Dthreads=%THREADS% -Dmaven.test.failure.ignore=true
call mvn allure:serve
endlocal
