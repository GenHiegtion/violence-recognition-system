#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WEB_SCRIPT="${ROOT_DIR}/web-service/run.sh"
AI_DIR="${ROOT_DIR}/ai-service"

declare -a pids=()

cleanup() {
  echo ""
  echo "Stopping all services..."
  for pid in "${pids[@]:-}"; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
  done
  wait "${pids[@]:-}" 2>/dev/null || true
  echo "All services stopped."
}

trap cleanup EXIT INT TERM

if [[ ! -f "$WEB_SCRIPT" ]]; then
  echo "web-service launcher not found: $WEB_SCRIPT"
  exit 1
fi

if [[ ! -d "$AI_DIR" ]]; then
  echo "ai-service directory not found: $AI_DIR"
  exit 1
fi

echo "Starting web services..."
(
  cd "${ROOT_DIR}/web-service"
  bash ./run.sh
) &
WEB_PID=$!
pids+=("$WEB_PID")

sleep 2
if ! kill -0 "$WEB_PID" >/dev/null 2>&1; then
  echo "web-service launcher exited early. Check Maven/JDK installation and logs."
  exit 1
fi

echo "Starting AI service..."
if command -v uv >/dev/null 2>&1; then
  (
    cd "$AI_DIR"
    uv run main.py
  ) &
elif command -v uvicorn >/dev/null 2>&1; then
  (
    cd "$AI_DIR"
    uvicorn main:app --host 0.0.0.0 --port 8000
  ) &
elif command -v python3 >/dev/null 2>&1; then
  (
    cd "$AI_DIR"
    python3 -m uvicorn main:app --host 0.0.0.0 --port 8000
  ) &
else
  echo "Cannot start AI service. Install uv, uvicorn, or python3."
  exit 1
fi
AI_PID=$!
pids+=("$AI_PID")

sleep 2
if ! kill -0 "$AI_PID" >/dev/null 2>&1; then
  echo "AI service exited early. Check Python dependencies and logs."
  exit 1
fi

echo ""
echo "All services started."
echo "- API Gateway: http://localhost:8080"
echo "- Pattern Service: http://localhost:8081"
echo "- Video Ingestion Service: http://localhost:8082"
echo "- User Service: http://localhost:8083"
echo "- AI Service: http://localhost:8000"
echo ""
echo "Press Ctrl+C to stop all services."

wait "${pids[@]}"
