# Violence Recognition Web Services

This folder contains non-AI microservices implemented with Spring Boot:

- `api-gateway` (Spring Cloud Gateway)
- `pattern-service` (violence pattern management + confidence threshold)
- `video-ingestion-service` (stream/file ingestion and frame extraction simulation)
- `user-service` (register, login, logout, MEMBER/ADMIN authorization)

## Routing Map

- Gateway: `http://localhost:8080`
- Pattern service (direct): `http://localhost:8081`
- Video ingestion service (direct): `http://localhost:8082`
- User service (direct): `http://localhost:8083`

Through gateway:

- `POST /api/patterns`
- `GET /api/patterns`
- `GET /api/patterns/thresholds`
- `POST /api/ingestion/upload`
- `POST /api/ingestion/stream`
- `GET /api/ingestion/models`
- `GET /api/ingestion/recognitions`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/users` (ADMIN)

Entity schema now follows UML:

- User: `id`, `username`, `password`, `fullName`, `role`
- VioPattern: `id`, `name`, `sevLevel`, `threshold`, `file`
- Model: `id`, `name`, `pt`
- Recognition: `id`, `result`, `file`, `date`, `confidenceScore`, `model`, `vioPattern` (by id), `user` (by id)

## Run Services Individually

From `web-service`:

```bash
mvn -pl pattern-service spring-boot:run
mvn -pl video-ingestion-service spring-boot:run
mvn -pl user-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

## Run All Java Services

```bash
./run.sh
```

## Environment Notes

- If `javac` is missing, `run.sh` auto-downloads JDK 21 to `web-service/.local/jdk-21`.
- If local `mvn` is missing, `run.sh` falls back to `./mvnw`.
- `./mvnw` auto-downloads Maven (internet + curl/wget required).
- If Maven download is blocked, set `MAVEN_DOWNLOAD_URL` to one reachable mirror URL.
