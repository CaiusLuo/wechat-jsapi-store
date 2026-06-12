#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$SCRIPT_DIR"
BACKEND_DIR="$REPO_DIR/backend"
FRONTEND_DIR="$REPO_DIR/frontend"
SQL_FILE="$BACKEND_DIR/src/main/resources/sql/init.sql"
RELEASE_ROOT="${RELEASE_ROOT:-$REPO_DIR/release}"
RELEASE_DIR="$RELEASE_ROOT/wechat-jsapi-store"
ZIP_FILE="$RELEASE_ROOT/wechat-jsapi-store.zip"

if ! command -v pnpm >/dev/null 2>&1; then
  echo "[ERROR] pnpm is required"
  exit 1
fi

if ! command -v zip >/dev/null 2>&1; then
  echo "[ERROR] zip is required"
  exit 1
fi

(
  cd "$BACKEND_DIR"
  ./mvnw clean package -DskipTests
)

(
  cd "$FRONTEND_DIR"
  pnpm install --frozen-lockfile
  pnpm run build
)

JAR_FILE="$(ls -t "$BACKEND_DIR"/target/*.jar | head -n 1)"
if [ ! -f "$JAR_FILE" ]; then
  echo "[ERROR] Backend jar not found"
  exit 1
fi

rm -rf "$RELEASE_DIR" "$ZIP_FILE"
mkdir -p "$RELEASE_DIR/backend" "$RELEASE_DIR/frontend" "$RELEASE_DIR/sql"

cp "$JAR_FILE" "$RELEASE_DIR/backend/app.jar"
cp -R "$FRONTEND_DIR/dist/." "$RELEASE_DIR/frontend/"
cp "$SQL_FILE" "$RELEASE_DIR/sql/init.sql"

cat >"$RELEASE_DIR/README.txt" <<'EOF'
WeChat JSAPI Store deployment package

Suggested deployment layout:
- frontend/* -> /opt/wechat-jsapi-store/public/
- backend/app.jar -> /opt/wechat-jsapi-store/backend/app.jar
- runtime uploads -> /opt/wechat-jsapi-store/uploads/

Security:
- Provide all secrets through environment variables or a secret manager.
- Do not place certificates, private keys, production configuration, or database dumps in this package.

Database:
- sql/init.sql is only for initializing a brand-new empty environment.
- Existing databases must use separately reviewed migration scripts.
EOF

(
  cd "$RELEASE_ROOT"
  zip -qr "$ZIP_FILE" "wechat-jsapi-store"
)

echo "[OK] Release directory: $RELEASE_DIR"
echo "[OK] Release archive: $ZIP_FILE"
