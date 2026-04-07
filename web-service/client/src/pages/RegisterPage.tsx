import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { getErrorMessage } from '../services/http'

export function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()

  const [username, setUsername] = useState('')
  const [fullName, setFullName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await register({ username, fullName, password })
      navigate('/login', {
        replace: true,
        state: {
          username,
          registered: true,
        },
      })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="card auth-card">
      <div className="card-header">
        <h2>Register</h2>
        <p>Create a member account for the management console.</p>
      </div>

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
            placeholder="4-50 characters"
          />
        </label>

        <label>
          Full name
          <input
            value={fullName}
            onChange={(event) => setFullName(event.target.value)}
            minLength={2}
            maxLength={150}
            required
            placeholder="Your full name"
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
            pattern="^(?=.*[A-Za-z])(?=.*\\d).+$"
            title="Password must contain at least one letter and one digit"
            required
            placeholder="At least 8 characters, letters and digits"
          />
        </label>

        <button type="submit" disabled={isSubmitting} className="primary-button">
          {isSubmitting ? 'Creating account...' : 'Register'}
        </button>
      </form>

      <p className="muted-text">
        Already have an account? <Link to="/login">Back to login</Link>
      </p>
    </section>
  )
}
