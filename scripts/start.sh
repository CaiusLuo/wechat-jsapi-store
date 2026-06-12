#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RUNTIME_DIR="${RUNTIME_DIR:-$REPO_DIR/.runtime}"
UPLOAD_DIR="${UPLOAD_BASE_DIR:-$RUNTIME_DIR/uploads}"
LOG_DIR="${LOG_DIR:-$RUNTIME_DIR/logs}"
PID_FILE="${PID_FILE:-$RUNTIME_DIR/backend.pid}"
SERVER_PORT="${SERVER_PORT:-8080}"

if [ -f "$REPO_DIR/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "$REPO_DIR/.env"
  set +a
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export DB_HOST="${DB_HOST:-127.0.0.1}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-wechat_store}"
export H5_AUTH_TTL_SECONDS="${H5_AUTH_TTL_SECONDS:-604800}"
export ADMIN_TOKEN_TTL_SECONDS="${ADMIN_TOKEN_TTL_SECONDS:-604800}"
export APP_CORS_ALLOWED_ORIGINS="${APP_CORS_ALLOWED_ORIGINS:-http://localhost:5173,http://127.0.0.1:5173}"
export SPRINGDOC_ENABLED="${SPRINGDOC_ENABLED:-true}"
export UPLOAD_BASE_DIR="$UPLOAD_DIR"
export SERVER_PORT

required_vars=(
  DB_USERNAME
  DB_PASSWORD
  ADMIN_USERNAME
  ADMIN_PASSWORD
  ADMIN_TOKEN_SECRET
  JWT_SECRET
  WECHAT_MP_APP_ID
  WECHAT_MP_SECRET
  WECHAT_MP_OAUTH_CALLBACK_URL
  WECHAT_PAY_APP_ID
  WECHAT_PAY_MCH_ID
  WECHAT_PAY_API_V3_KEY
  WECHAT_PAY_PRIVATE_KEY_PATH
  WECHAT_PAY_SERIAL_NO
  WECHAT_PAY_NOTIFY_URL
)

for var in "${required_vars[@]}"; do
  if [ -z "${!var:-}" ]; then
    echo "[ERROR] Required environment variable is missing: $var"
    exit 1
  fi
done

if ! command -v pnpm >/dev/null 2>&1; then
  echo "[ERROR] pnpm is required"
  exit 1
fi

mkdir -p "$UPLOAD_DIR" "$LOG_DIR" "$RUNTIME_DIR/public" "$RUNTIME_DIR/backend"

(
  cd "$REPO_DIR/backend"
  ./mvnw clean package -DskipTests
)

(
  cd "$REPO_DIR/frontend"
  pnpm install --frozen-lockfile
  pnpm run build
)

JAR_FILE="$(ls -t "$REPO_DIR"/backend/target/*.jar | head -n 1)"
cp "$JAR_FILE" "$RUNTIME_DIR/backend/app.jar"
rm -rf "$RUNTIME_DIR/public"
cp -R "$REPO_DIR/frontend/dist" "$RUNTIME_DIR/public"

if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "${OLD_PID:-}" ] && kill -0 "$OLD_PID" 2>/dev/null; then
    echo "[ERROR] Backend is already running with pid $OLD_PID"
    exit 1
  fi
  rm -f "$PID_FILE"
fi

nohup java -jar "$RUNTIME_DIR/backend/app.jar" \
  >"$LOG_DIR/backend.log" 2>"$LOG_DIR/backend-error.log" &

BACKEND_PID=$!
echo "$BACKEND_PID" >"$PID_FILE"

echo "[OK] Backend started on http://127.0.0.1:${SERVER_PORT}"
echo "[OK] Frontend build: $RUNTIME_DIR/public"
echo "[INFO] Configure a local web server with deploy/nginx.example.conf or run pnpm preview."
echo "[INFO] Runtime logs: $LOG_DIR"
