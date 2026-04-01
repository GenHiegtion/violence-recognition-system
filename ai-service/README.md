# AI Inference Service (PyTorch MoViNet)

Python microservice for `Violence` / `Non-violence` recognition.

## Features

- `POST /api/ai/infer` supports 3 scoring modes:
	- Receive `violence_score` from request.
	- Run MoViNet PyTorch if `video_path` is available.
	- Fallback heuristic if the model or video is unavailable.
- Fetch threshold values from `pattern-service` (`/api/patterns/thresholds`).
- Notebook fine-tune MoViNet: `movinet_finetuning_pytorch.ipynb`.

## Main Files

- `main.py`: initialize the FastAPI app and include the router.
- `endpoints.py`: define API endpoints.
- `model_inference.py`: loader + infer MoViNet PyTorch.
- `pattern_client.py`: call threshold API from pattern-service.
- `schemas.py`: request/response models.
- `settings.py`: read environment variables.

## Run Locally

```bash
uv sync
uv run main.py
```

Or:

```bash
uv sync
uv run uvicorn main:app --host 0.0.0.0 --port 8000 --reload
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

Notebook: `movinet_finetuning_pytorch.ipynb`

The notebook will:

- Read the dataset from `real-life-violence-dataset/Violence` and `real-life-violence-dataset/NonViolence`.
- Fine-tune MoViNet A0 with PyTorch 2.6.0.
- Save checkpoint to `weights/movinet_a0_violence.pt` for reuse during inference.
