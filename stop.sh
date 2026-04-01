#!/usr/bin/env bash
set -euo pipefail

DEFAULT_PORTS=(8000 8080 8081 8082 8083)
TARGET_PORTS=("$@")

if [[ ${#TARGET_PORTS[@]} -eq 0 ]]; then
  TARGET_PORTS=("${DEFAULT_PORTS[@]}")
fi

if ! command -v lsof >/dev/null 2>&1 && ! command -v fuser >/dev/null 2>&1 && ! command -v ss >/dev/null 2>&1; then
  echo "Need at least one tool: lsof, fuser, or ss"
  exit 1
fi

collect_pids_for_port() {
  local port="$1"

  if command -v lsof >/dev/null 2>&1; then
    lsof -t -iTCP:"$port" -sTCP:LISTEN 2>/dev/null | sort -u || true
    return
  fi

  if command -v fuser >/dev/null 2>&1; then
    fuser -n tcp "$port" 2>/dev/null | tr ' ' '\n' | grep -E '^[0-9]+$' | sort -u || true
    return
  fi

  ss -ltnp "sport = :$port" 2>/dev/null | awk -F'pid=' '/pid=/{split($2,a,","); print a[1]}' | grep -E '^[0-9]+$' | sort -u || true
}

kill_pid() {
  local pid="$1"

  if ! kill -0 "$pid" >/dev/null 2>&1; then
    return
  fi

  kill "$pid" >/dev/null 2>&1 || true
  sleep 1

  if kill -0 "$pid" >/dev/null 2>&1; then
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi
}

stopped_any=0

for port in "${TARGET_PORTS[@]}"; do
  pids="$(collect_pids_for_port "$port" | tr '\n' ' ')"

  if [[ -z "${pids// }" ]]; then
    echo "Port $port: no listening process"
    continue
  fi

  echo "Port $port: stopping PIDs ${pids}"
  for pid in $pids; do
    kill_pid "$pid"
  done
  stopped_any=1

done

if [[ "$stopped_any" -eq 0 ]]; then
  echo "No processes were stopped."
else
  echo "Stop routine completed."
fi
