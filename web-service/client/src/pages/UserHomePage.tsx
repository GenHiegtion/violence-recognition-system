import { useNavigate } from 'react-router-dom'

export function UserHomePage() {
  const navigate = useNavigate()

  return (
    <section className="manager-home">
      <article className="panel">
        <h2>User Home</h2>
        <p className="panel-subtitle">
          Choose a module to continue your workflow.
        </p>

        <div className="feature-grid">
          <button
            type="button"
            className="feature-tile"
            onClick={() => navigate('/recognition')}
          >
            <strong>Violence Recognition</strong>
            <span>Open recognition workspace and monitor incoming detection flows.</span>
          </button>
        </div>
      </article>
    </section>
  )
}
