import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../services/http'
import { patternApi } from '../services/patternApi'
import type { VioPatternResponse } from '../types/pattern'

const VIDEO_EXTENSIONS = ['.mp4', '.webm', '.ogg', '.mov', '.m4v', '.avi', '.mkv']

function resolveFileUrl(filePath: string): string {
  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    return filePath
  }
  if (filePath.startsWith('/')) {
    return filePath
  }
  return `/${filePath}`
}

function isVideoPath(filePath: string): boolean {
  const lowerPath = filePath.toLowerCase()
  return VIDEO_EXTENSIONS.some((ext) => lowerPath.endsWith(ext))
}

export function PatternDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [pattern, setPattern] = useState<VioPatternResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const patternId = Number(id)
    if (Number.isNaN(patternId)) {
      setError('Invalid pattern id.')
      setIsLoading(false)
      return
    }

    const loadPattern = async () => {
      try {
        const result = await patternApi.findById(patternId)
        setPattern(result)
      } catch (err) {
        setError(getErrorMessage(err))
      } finally {
        setIsLoading(false)
      }
    }

    void loadPattern()
  }, [id])

  if (isLoading) {
    return (
      <section className="simple-page">
        <article className="panel">
          <p>Loading pattern detail...</p>
        </article>
      </section>
    )
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Pattern Detail</h2>
        {error && <p className="banner error">{error}</p>}

        {pattern && (
          <>
            <dl className="detail-grid">
              <div>
                <dt>Name</dt>
                <dd>{pattern.name}</dd>
              </div>
              <div>
                <dt>Severity Level</dt>
                <dd>{pattern.sevLevel}</dd>
              </div>
              <div>
                <dt>Threshold</dt>
                <dd>{pattern.threshold.toFixed(2)}</dd>
              </div>
            </dl>

            <section className="media-panel">
              <h3>Uploaded Video</h3>
              {pattern.file ? (
                isVideoPath(pattern.file) ? (
                  <video
                    className="detail-video"
                    controls
                    preload="metadata"
                    src={resolveFileUrl(pattern.file)}
                  >
                    Your browser does not support video playback.
                  </video>
                ) : (
                  <p className="muted-text">
                    Uploaded file is not recognized as a video format.
                  </p>
                )
              ) : (
                <p className="muted-text">No uploaded video for this pattern.</p>
              )}
            </section>
          </>
        )}

        <div className="panel-actions panel-actions-spread">
          <button
            type="button"
            className="secondary-button"
            onClick={() => navigate('/patterns')}
          >
            Back
          </button>
          {pattern && (
            <button
              type="button"
              className="primary-button"
              onClick={() => navigate(`/patterns/${pattern.id}/edit`)}
            >
              Modify
            </button>
          )}
        </div>
      </article>
    </section>
  )
}
