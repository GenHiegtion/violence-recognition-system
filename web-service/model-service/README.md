# Model Service

Model catalog service.

## Responsibilities

- Persist available AI models (`name`, `pt`).
- Sync model catalog from AI weights directory.
- Expose query APIs for model listing and model detail.

## Endpoints

- `GET /api/models`
- `GET /api/models/{id}`
