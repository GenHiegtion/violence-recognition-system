import { useLocation, useNavigate } from 'react-router-dom'
import type { RecognitionWorkflowState } from '../types/recognition'

interface RecognitionRouteState {
  workflow?: RecognitionWorkflowState
}

function formatDateTime(raw: string | undefined): string {
  if (!raw) {
    return 'N/A'
  }

  const parsed = new Date(raw)
  if (Number.isNaN(parsed.getTime())) {
    return raw
  }

  return parsed.toLocaleString('vi-VN')
}

function formatConfidence(raw: number | undefined, resultLabel: string | undefined): string {
  if (typeof raw !== 'number') {
    return 'N/A'
  }

  if (resultLabel?.toLowerCase() === 'non-violence' && raw <= 0.5) {
    return (1 - raw).toFixed(4)
  }

  return raw.toFixed(4)
}

export function ViolenceRecognitionPage() {
  const navigate = useNavigate()
  const location = useLocation()

  const routeState = location.state as RecognitionRouteState | null
  const workflow = routeState?.workflow
  const result = workflow?.recognitionResult

  if (!workflow || !result) {
    return (
      <section className="simple-page">
        <article className="panel">
          <h2>Violence Recognition</h2>
          <p className="banner error">
            Missing recognition result. Please complete video and model selection first.
          </p>

          <div className="panel-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={() => navigate('/recognition/video')}
            >
              Back
            </button>
          </div>
        </article>
      </section>
    )
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Violence Recognition</h2>
        <p className="panel-subtitle">
          Recognition result generated from selected video and selected model.
        </p>

        <section className="media-panel recognition-media-panel">
          <h3>Selected Video</h3>
          {workflow.previewUrl ? (
            <video className="detail-video" controls preload="metadata" src={workflow.previewUrl}>
              Your browser does not support video playback.
            </video>
          ) : (
            <p className="muted-text">No video preview available.</p>
          )}
          <p className="muted-text">{workflow.selectedFileName}</p>
        </section>

        <dl className="detail-grid recognition-result-grid">
          <div>
            <dt>Model</dt>
            <dd>{result.modelName || workflow.selectedModel?.name || 'N/A'}</dd>
          </div>
          <div>
            <dt>Result</dt>
            <dd>{result.result}</dd>
          </div>
          <div>
            <dt>Returned At</dt>
            <dd>{formatDateTime(result.date)}</dd>
          </div>
          <div>
            <dt>Confidence Score</dt>
            <dd>{formatConfidence(result.confidenceScore, result.result)}</dd>
          </div>
        </dl>

        <div className="panel-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => navigate('/recognition/models', { state: { workflow } })}
          >
            Back
          </button>
        </div>
      </article>
    </section>
  )
}
