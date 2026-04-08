# Recognition Service

Recognition execution and persistence service.

## Responsibilities

- Execute violence recognition by calling AI service.
- Fetch selected model metadata from model-service.
- Persist recognition results.

## Endpoints

- `POST /api/recognitions/upload`
- `POST /api/recognitions/execute`
- `GET /api/recognitions`
- `GET /api/recognitions/{id}`
