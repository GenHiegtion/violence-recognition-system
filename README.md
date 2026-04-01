# Violence Recognition System

This repository is organized as a microservice system with two groups:

- AI inference service (Python FastAPI) for Violence/Non-violence prediction.
- Web services (Java Spring Boot) for gateway and non-AI business domains.

## 1. Architecture Overview

### 1.1 Services and Responsibilities

- `api-gateway` (Spring Cloud Gateway)
  - Single entry point for client requests.
  - Routes traffic to downstream services.
- `pattern-service`
  - Manages `VioPattern` entity and confidence thresholds.
  - Exposes active thresholds consumed by AI inference.
- `video-ingestion-service`
  - Accepts uploaded video files or stream URLs.
  - Creates `Recognition` records and tracks related `Model` data.
- `user-service`
  - Handles register, login, logout, and role-based access.
  - Persists `User` entity in DB and uses in-memory auth sessions.
- `ai-service` (FastAPI)
  - Provides `/api/ai/infer` prediction endpoint.
  - Fetches dynamic thresholds from `pattern-service`.

### 1.2 Default Ports

- API Gateway: `8080`
- Pattern Service: `8081`
- Video Ingestion Service: `8082`
- User Service: `8083`
- AI Service: `8000`

### 1.3 Request Flow

1. Client calls the gateway on port `8080`.
2. Gateway routes by path:
   - `/api/patterns/**` -> pattern-service
   - `/api/ingestion/**` -> video-ingestion-service
   - `/api/auth/**`, `/api/users/**` -> user-service
   - `/api/ai/**`, `/health/ai` -> ai-service
3. During inference, ai-service calls `pattern-service /api/patterns/thresholds`.

## 2. Repository Structure

```text
violence-recognition-system/
  .vscode/
  ai-service/
    main.py
    pyproject.toml
    README.md
  web-service/
    pom.xml
    run.sh
    mvnw
    api-gateway/
    pattern-service/
    video-ingestion-service/
    user-service/
  start.sh
  README.md
```

## 3. Current Implementation Details

### 3.1 Multi-module Web Service

- Parent POM: `web-service/pom.xml`
- Modules:
  - `web-service/api-gateway`
  - `web-service/pattern-service`
  - `web-service/video-ingestion-service`
  - `web-service/user-service`

### 3.2 API Gateway Routing

Primary file: `web-service/api-gateway/src/main/resources/application.yml`

- Routes to pattern, ingestion, user, and AI services.
- Supports URL overrides through environment variables:
  - `PATTERN_SERVICE_URL`
  - `VIDEO_INGESTION_SERVICE_URL`
  - `USER_SERVICE_URL`
  - `AI_SERVICE_URL`

### 3.3 Pattern Management Service

Main files:

- `web-service/pattern-service/src/main/java/com/vrs/pattern/controller/PatternController.java`
- `web-service/pattern-service/src/main/java/com/vrs/pattern/service/PatternManagementService.java`
- `web-service/pattern-service/src/main/java/com/vrs/pattern/model/VioPattern.java`

Features:

- Pattern CRUD
- Active threshold export for AI (`GET /api/patterns/thresholds`)
- UML fields: `id`, `name`, `sevLevel`, `threshold`, `file`

### 3.4 Video Ingestion Service

Main files:

- `web-service/video-ingestion-service/src/main/java/com/vrs/ingestion/controller/IngestionController.java`
- `web-service/video-ingestion-service/src/main/java/com/vrs/ingestion/service/IngestionService.java`

Features:

- `POST /api/ingestion/upload` (multipart)
- `POST /api/ingestion/stream` (JSON)
- `GET /api/ingestion/jobs`
- `GET /api/ingestion/models`
- `GET /api/ingestion/recognitions`

Current behavior is in-memory ingestion simulation (no message broker yet).

UML entities implemented in this service:

- `Model`: `id`, `name`, `pt`
- `Recognition`: `id`, `result`, `file`, `date`, `confidenceScore`, `model`, `userId`, `vioPatternId`

### 3.5 User Service

Main files:

- `web-service/user-service/src/main/java/com/vrs/user/controller/AuthController.java`
- `web-service/user-service/src/main/java/com/vrs/user/controller/UserController.java`
- `web-service/user-service/src/main/java/com/vrs/user/config/AuthInterceptor.java`
- `web-service/user-service/src/main/java/com/vrs/user/service/UserAccountService.java`

Features:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/users` (ADMIN only)

UML fields implemented for user entity: `id`, `username`, `password`, `fullName`, `role`.

Security/session details in this version:

- Password hashing with BCrypt.
- In-memory session token store (not JWT yet).
- Default bootstrapped admin account: `admin / Admin1234`.
- Public register endpoint always creates `USER` role (prevents privilege escalation).

### 3.6 AI Inference Service

Main files:

- `ai-service/main.py`
- `ai-service/pyproject.toml`

Endpoints:

- `POST /api/ai/infer`
- `GET /health`
- `GET /health/ai`

Inference logic in current version:

- Uses request `violence_score` if provided.
- Otherwise computes heuristic score from `frames_count`.
- Loads threshold by `pattern_code` from pattern-service.
- Returns `Violence` or `Non-violence` based on score vs threshold.

## 4. Running the System

### 4.1 Prerequisites

- Java runtime (JRE) is enough to bootstrap local JDK automatically
- Python 3.12+
- One of: `uv`, `uvicorn`, or `python3 -m uvicorn`
- Optional local Maven (`mvn`)

Maven note:

- If `mvn` is missing, `web-service/run.sh` now falls back to `web-service/mvnw`.
- `web-service/mvnw` downloads Maven automatically (requires internet + curl/wget).
- If default download URLs are blocked, set `MAVEN_DOWNLOAD_URL` to a reachable mirror.

JDK note:

- If `javac` is missing, `web-service/run.sh` auto-downloads JDK 21 into `web-service/.local/jdk-21`.
- This avoids requiring `sudo` on development machines.

Quick environment checks:

```bash
java -version
javac -version
mvn -version
```

Optional system-wide install (Ubuntu/Debian) if you prefer not to use local bootstrap:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
```

If `mvn` is missing and you do not want wrapper download:

```bash
sudo apt install -y maven
```

Example mirror override:

```bash
export MAVEN_DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.tar.gz"
```

### 4.2 Start All Services with One Command

From repository root:

```bash
chmod +x start.sh
./start.sh
```

What `start.sh` does:

- Starts `web-service/run.sh` (all 4 Java services)
- Starts `ai-service`
- Handles shutdown for all spawned processes with Ctrl+C

### 4.3 Stop Lingering Processes by Port

If any process still keeps ports busy after an unexpected interruption:

```bash
chmod +x stop.sh
./stop.sh
```

Default stop ports: `8000`, `8080`, `8081`, `8082`, `8083`.

You can also pass custom ports:

```bash
./stop.sh 8080 8000
```

### 4.4 Start Services Separately

Web only:

```bash
cd web-service
bash run.sh
```

AI only:

```bash
cd ai-service
uv run main.py
```

## 5. Database Configuration

### 5.1 Current State

Pattern, user, and video-ingestion services now use a database.

Configuration files:

- `web-service/pattern-service/src/main/resources/application.yml`
- `web-service/user-service/src/main/resources/application.yml`
- `web-service/video-ingestion-service/src/main/resources/application.yml`

Default database is in-memory H2:

- URL: `jdbc:h2:mem:patterndb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: empty
- Driver: `org.h2.Driver`

### 5.2 Switch to MySQL

Set environment variables before starting pattern-service:

```bash
export DB_URL="jdbc:mysql://localhost:3306/violence_patterns?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
```

Then restart services.

## 6. Quick API Tests

### 6.1 Create a Pattern

```bash
curl -X POST http://localhost:8080/api/patterns \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fighting",
    "sevLevel": 3,
    "threshold": 0.65,
    "file": "fighting-reference.mp4"
  }'
```

### 6.2 Register and Login

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user01","fullName":"User One","password":"User12345"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user01","password":"User12345"}'
```

### 6.3 Run Inference

```bash
curl -X POST http://localhost:8080/api/ai/infer \
  -H "Content-Type: application/json" \
  -d '{
    "source_id": "cam-01",
    "pattern_code": "violence",
    "frames_count": 24
  }'
```

## 7. Current Limitations and Next Improvements

- User auth is in-memory; move to JWT + refresh token + persistent storage.
- Ingestion is simulated; add Kafka or RabbitMQ for scale.
- AI scoring is heuristic; replace with production MoViNet pipeline.
- No full-stack docker-compose yet; add one for easier local setup.

## 8. What Was Fixed in This Iteration

- Added single-command root launcher: `start.sh`.
- Added root stop utility: `stop.sh`.
- Added Maven fallback wrapper: `web-service/mvnw`.
- Updated web launcher to use `mvn` or fallback `mvnw` automatically.
- Converted project documentation from Vietnamese to English.
