import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function navClassName({ isActive }: { isActive: boolean }) {
  return isActive ? 'nav-link nav-link-active' : 'nav-link'
}

export function AppLayout() {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <div className="hero-background" aria-hidden="true" />
      <header className="topbar">
        <div className="brand">
          <p className="brand-eyebrow">Violence Recognition System</p>
          <h1>Management Console</h1>
        </div>

        <div className="top-right-cluster">
          {isAuthenticated && (
            <span className="role-badge">{user?.role === 'ADMIN' ? 'Admin' : 'Member'}</span>
          )}

          <nav className="top-nav" aria-label="Main navigation">
          {isAuthenticated ? (
            <>
              <NavLink to="/manager-home" className={navClassName}>
                Home
              </NavLink>
              <NavLink to="/patterns" className={navClassName}>
                Violence Patterns
              </NavLink>
              <button className="ghost-button" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={navClassName}>
                Login
              </NavLink>
              <NavLink to="/register" className={navClassName}>
                Register
              </NavLink>
            </>
          )}
          </nav>
        </div>
      </header>

      <section className="session-ribbon">
        {isAuthenticated && user ? (
          <p>Manager workspace ready.</p>
        ) : (
          <p>Create an account or sign in to open manager workflows.</p>
        )}
      </section>

      <main className="content-panel">
        <Outlet />
      </main>
    </div>
  )
}
