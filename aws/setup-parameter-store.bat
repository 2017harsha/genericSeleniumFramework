@echo off
REM ---------------------------------------------------------------------------
REM One-time setup: store PROD test credentials in AWS SSM Parameter Store.
REM Requires AWS CLI v2 configured with an admin/devops profile (aws configure).
REM Usage:  setup-parameter-store.bat <prod-username> <prod-password> [region]
REM ---------------------------------------------------------------------------
setlocal
if "%~2"=="" (
  echo Usage: %~nx0 ^<prod-username^> ^<prod-password^> [region]
  exit /b 1
)
set REGION=%~3
if "%REGION%"=="" set REGION=ap-south-1

aws ssm put-parameter --region %REGION% --name "/selenium-framework/prod/app.username" --type SecureString --value "%~1" --overwrite
aws ssm put-parameter --region %REGION% --name "/selenium-framework/prod/app.password" --type SecureString --value "%~2" --overwrite

echo.
echo Verify (values stay encrypted unless --with-decryption):
aws ssm get-parameters-by-path --region %REGION% --path "/selenium-framework/prod" --query "Parameters[].Name"
endlocal
