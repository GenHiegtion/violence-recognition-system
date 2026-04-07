import { useNavigate } from 'react-router-dom'

export function StatisticsPage() {
  const navigate = useNavigate()

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Statistics</h2>
        <p className="panel-subtitle">
          This area is prepared for violence recognition metrics and trend analytics.
        </p>
        <p className="muted-text">
          Add charts and report widgets when statistics endpoints are available.
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
