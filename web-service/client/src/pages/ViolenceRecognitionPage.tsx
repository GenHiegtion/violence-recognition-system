import { useNavigate } from 'react-router-dom'

export function ViolenceRecognitionPage() {
  const navigate = useNavigate()

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Violence Recognition</h2>
        <p className="panel-subtitle">
          This module is reserved for recognition operations and live stream analysis.
        </p>
        <p className="muted-text">
          You can plug the real recognition dashboard here once the API integration is ready.
        </p>

        <div className="panel-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => navigate('/manager-home')}
          >
            Back
          </button>
        </div>
      </article>
    </section>
  )
}
