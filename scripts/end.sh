#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RUNTIME_DIR="${RUNTIME_DIR:-$REPO_DIR/.runtime}"
PID_FILE="${PID_FILE:-$RUNTIME_DIR/backend.pid}"

if [ ! -f "$PID_FILE" ]; then
  echo "[INFO] Backend PID file not found"
  exit 0
fi

PID="$(cat "$PID_FILE" || true)"
if [ -n "${PID:-}" ] && kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  echo "[OK] Backend stopped: pid=$PID"
else
  echo "[INFO] Backend process is not running"
fi

rm -f "$PID_FILE"
