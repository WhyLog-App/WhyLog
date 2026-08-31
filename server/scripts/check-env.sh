#!/usr/bin/env bash
# .env에 필수 값이 채워졌는지 검사합니다. dev.sh가 자동으로 호출하며, 단독 실행도 가능합니다.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo ".env가 없습니다. cp .env.example .env 로 만든 뒤 값을 채워주세요." >&2
  exit 1
fi

set -a
source .env
set +a

# 서버 기동과 핵심 기능에 필요한 값을 검사합니다.
# DEV_REDIS_PASSWORD는 필수지만 로컬 Redis에 비밀번호가 없으면 빈 값이 정상이라 제외합니다.
REQUIRED_VARS=(
  DEV_DB_URL
  DEV_DB_USERNAME
  DEV_DB_PASSWORD
  DEV_REDIS_HOST
  DEV_REDIS_PORT
  CORS_ALLOWED_ORIGINS
  JWT_SECRET
  GMAIL_SMTP_USERNAME
  GMAIL_SMTP_APP_PASSWORD
  EMAIL_VERIFICATION_CODE_SECRET
  GITHUB_TOKEN_ENCRYPTION_KEY
  AWS_S3_BUCKET
  AWS_S3_ACCESS_KEY
  AWS_S3_SECRET_KEY
  FAST_API_BASE_URL
)

missing=()
for name in "${REQUIRED_VARS[@]}"; do
  value="${!name:-}"
  if [ -z "$value" ]; then
    missing+=("$name")
  fi
done

if [ "${#missing[@]}" -gt 0 ]; then
  echo ".env에 아래 필수 값이 비어 있습니다:" >&2
  for name in "${missing[@]}"; do
    echo "  - $name" >&2
  done
  exit 1
fi

echo "필수 환경변수 확인 완료."
