# AI Inference Service (PyTorch MoViNet)

Python microservice for `Violence` / `Non-violence` recognition.

## Features

- `POST /api/ai/infer` supports 3 scoring modes:
	- Receive `violence_score` from request.
	- Run MoViNet PyTorch if `video_path` is available.
	- Fallback heuristic if the model or video is unavailable.
- Fetch threshold values from `pattern-service` (`/api/patterns/thresholds`).
- Notebook fine-tune MoViNet: `movinet_finetuning.ipynb`.

## Main Files

- `main.py`: compatibility entrypoint for local run.
- `src/app.py`: FastAPI app, middleware, lifecycle hooks.
- `src/api/routes/inference.py`: inference endpoints.
- `src/services/model_inference.py`: MoViNet loader/inference.
- `src/services/pattern_client.py`: threshold client to pattern-service.
- `src/schemas/inference.py`: request/response schemas.
- `src/core/settings.py`: environment configuration.

## Run Locally

```bash
uv sync
uv run main.py
```

Or:

```bash
uv sync
uv run uvicorn src.app:app --host 0.0.0.0 --port 8000 --reload
```

## Request Example

```bash
curl -X POST http://localhost:8000/api/ai/infer \
	-H "Content-Type: application/json" \
	-d '{
		"source_id": "cam-01",
		"pattern_code": "violence",
		"frames_count": 32,
		"video_path": "real-life-violence-dataset/Violence/V_1.mp4"
	}'
```

## Environment Variables

- `PATTERN_SERVICE_URL` (default: `http://localhost:8081`)
- `DEFAULT_VIOLENCE_THRESHOLD` (default: `0.60`)
- `MOVINET_CHECKPOINT_PATH` (default: `ai-service/weights/movinet_a0_violence.pt`)
- `MOVINET_MODEL_NAME` (default: `A0`)
- `MOVINET_IMAGE_SIZE` (default: `172`)
- `MOVINET_NUM_FRAMES` (default: `32`)
- `MOVINET_DEVICE` (default: `auto`, selectable: `cpu`/`cuda`/`mps`)

## Fine-tuning Notebook

Notebook: `movinet_finetuning.ipynb`

The notebook will:

- Read the dataset from `real-life-violence-dataset/Violence` and `real-life-violence-dataset/NonViolence`.
- Fine-tune MoViNet A0 with PyTorch 2.6.0.
- Save checkpoint to `weights/movinet_a0_violence.pt` for reuse during inference.
