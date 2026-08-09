#!/usr/bin/env bash
# Docker Compose로 MySQL/Redis를 띄우고 .env를 로드한 뒤 서버를 실행합니다.
set -euo pipefail
cd "$(dirname "$0")/.."

./scripts/check-env.sh

docker compose up -d --wait

set -a
source .env
set +a

exec ./gradlew bootRun
