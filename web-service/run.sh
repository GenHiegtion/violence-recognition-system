#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN_WRAPPER="${PROJECT_DIR}/mvnw"
LOCAL_JDK_DIR="${PROJECT_DIR}/.local/jdk-21"
LOCAL_CACHE_DIR="${PROJECT_DIR}/.local/cache"
JDK_DOWNLOAD_URL="${JDK_DOWNLOAD_URL:-https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse}"

cd "$PROJECT_DIR"

activate_local_jdk() {
  export JAVA_HOME="$LOCAL_JDK_DIR"
  export PATH="$LOCAL_JDK_DIR/bin:$PATH"
}

download_local_jdk() {
  local archive_path="${LOCAL_CACHE_DIR}/jdk21.tar.gz"
  local extract_dir="${LOCAL_CACHE_DIR}/jdk-extract"

  mkdir -p "$LOCAL_CACHE_DIR"
  rm -rf "$extract_dir"
  mkdir -p "$extract_dir"

  echo "Java compiler not found. Downloading local JDK 21 (no sudo required)..."

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$JDK_DOWNLOAD_URL" -o "$archive_path"
  elif command -v wget >/dev/null 2>&1; then
    wget -qO "$archive_path" "$JDK_DOWNLOAD_URL"
  else
    echo "Neither curl nor wget is available to download JDK."
    exit 1
  fi

  tar -xzf "$archive_path" -C "$extract_dir"

  local extracted_jdk
  extracted_jdk="$(find "$extract_dir" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
  if [[ -z "$extracted_jdk" ]]; then
    echo "Failed to unpack local JDK archive."
    exit 1
  fi

  rm -rf "$LOCAL_JDK_DIR"
  mv "$extracted_jdk" "$LOCAL_JDK_DIR"
}

ensure_jdk() {
  if command -v java >/dev/null 2>&1 && command -v javac >/dev/null 2>&1; then
    return
  fi

  if [[ -x "${LOCAL_JDK_DIR}/bin/javac" ]]; then
    activate_local_jdk
    return
  fi

  download_local_jdk
  activate_local_jdk

  if ! command -v java >/dev/null 2>&1 || ! command -v javac >/dev/null 2>&1; then
    echo "JDK setup failed. Please install a full JDK manually."
    exit 1
  fi
}

ensure_jdk

if command -v mvn >/dev/null 2>&1; then
  MVN_CMD="mvn"
elif [[ -x "$MVN_WRAPPER" ]]; then
  MVN_CMD="$MVN_WRAPPER"
else
  echo "Neither mvn nor mvnw is available."
  echo "Install Maven, or keep web-service/mvnw in this repository."
  exit 1
fi

echo "Preparing Maven runtime..."
if ! "$MVN_CMD" -v >/dev/null; then
  echo "Failed to initialize Maven runtime."
  echo "If your network blocks Maven download, set MAVEN_DOWNLOAD_URL to a reachable mirror."
  exit 1
fi

declare -a pids=()

start_service() {
  local module="$1"
  echo "Starting ${module}..."
  (
    cd "${module}"
    "$MVN_CMD" spring-boot:run
  ) &
  pids+=("$!")
}

cleanup() {
  echo "Stopping services..."
  for pid in "${pids[@]:-}"; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill "$pid" >/dev/null 2>&1 || true
    fi
  done
}

trap cleanup EXIT INT TERM

start_service pattern-service
sleep 2
start_service video-ingestion-service
sleep 2
start_service user-service
sleep 2
start_service api-gateway

echo "All Java services started. Press Ctrl+C to stop."
wait
