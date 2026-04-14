import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { getErrorMessage } from '../services/http'

type LoginLocationState = {
  username?: string
  registered?: boolean
  from?: string
}

export function LoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { login } = useAuth()

  const state = (location.state || {}) as LoginLocationState

  const [username, setUsername] = useState(state.username ?? '')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      const account = await login({ username, password })
      const defaultHome = account.role === 'ADMIN' ? '/manager-home' : '/user-home'
      navigate(state.from ?? defaultHome, { replace: true })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="card auth-card">
      <div className="card-header">
        <h2>Login</h2>
        <p>Authenticate with your username and password.</p>
      </div>

      {state.registered && (
        <p className="banner success">Registration successful. Please login.</p>
      )}
      {error && <p className="banner error">{error}</p>}

      <form onSubmit={handleSubmit} className="grid-form">
        <label>
          Username
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            minLength={4}
            maxLength={50}
            required
            autoComplete="username"
            placeholder="your-username"
          />
        </label>

        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={8}
            maxLength={100}
            required
            autoComplete="current-password"
            placeholder="Your password"
          />
        </label>

        <button type="submit" disabled={isSubmitting} className="primary-button">
          {isSubmitting ? 'Signing in...' : 'Login'}
        </button>
      </form>

      <p className="muted-text">
        New here? <Link to="/register">Create account</Link>
      </p>
    </section>
  )
}
