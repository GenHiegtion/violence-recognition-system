# Violence Recognition Web Services

This folder contains non-AI microservices implemented with Spring Boot:

- `api-gateway` (Spring Cloud Gateway)
- `model-service` (model catalog and weight mapping)
- `pattern-service` (violence pattern management + confidence threshold)
- `recognition-service` (recognition execution and result persistence)
- `user-service` (register, login, logout, MEMBER/ADMIN authorization)

## Routing Map

- Gateway: `http://localhost:8080`
- Pattern service (direct): `http://localhost:8081`
- User service (direct): `http://localhost:8083`
- Model service (direct): `http://localhost:8084`
- Recognition service (direct): `http://localhost:8085`

Through gateway:

- `POST /api/patterns`
- `GET /api/patterns`
- `GET /api/patterns/thresholds`
- `GET /api/models`
- `POST /api/recognitions/upload`
- `POST /api/recognitions/execute`
- `GET /api/recognitions`
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
mvn -pl model-service spring-boot:run
mvn -pl recognition-service spring-boot:run
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
