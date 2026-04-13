import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

interface HomeFeature {
  title: string
  description: string
  to: string
}

const FEATURES: HomeFeature[] = [
  {
    title: 'Violence Patterns',
    description: 'Manage violence pattern definitions, thresholds, and file metadata.',
    to: '/patterns',
  },
  {
    title: 'Violence Recognition',
    description: 'Open recognition workspace and monitor incoming detection flows.',
    to: '/recognition',
  },
  {
    title: 'Statistics',
    description: 'Review operational trends and key recognition indicators.',
    to: '/statistics',
  },
]

export function ManagerHomePage() {
  const navigate = useNavigate()
  const { isAdmin } = useAuth()

  const features = isAdmin
    ? FEATURES
    : FEATURES.filter((feature) => feature.to === '/recognition')

  return (
    <section className="manager-home">
      <article className="panel">
        <h2>Manager Home</h2>
        <p className="panel-subtitle">
          {isAdmin
            ? 'Choose a module to continue your administrative workflow.'
            : 'Choose a module to continue your workflow.'}
        </p>

        <div className="feature-grid">
          {features.map((feature) => (
            <button
              key={feature.to}
              type="button"
              className="feature-tile"
              onClick={() => navigate(feature.to)}
            >
              <strong>{feature.title}</strong>
              <span>{feature.description}</span>
            </button>
          ))}
        </div>
      </article>
    </section>
  )
}
