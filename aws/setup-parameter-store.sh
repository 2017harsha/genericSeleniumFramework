#!/usr/bin/env bash
# One-time setup: store PROD test credentials in AWS SSM Parameter Store.
# Usage: ./setup-parameter-store.sh <prod-username> <prod-password> [region]
set -euo pipefail
USERNAME="${1:?username required}"
PASSWORD="${2:?password required}"
REGION="${3:-ap-south-1}"

aws ssm put-parameter --region "$REGION" --name "/selenium-framework/prod/app.username" \
  --type SecureString --value "$USERNAME" --overwrite
aws ssm put-parameter --region "$REGION" --name "/selenium-framework/prod/app.password" \
  --type SecureString --value "$PASSWORD" --overwrite

aws ssm get-parameters-by-path --region "$REGION" --path "/selenium-framework/prod" --query "Parameters[].Name"
