import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function FullPageStatus({ message }: { message: string }) {
  return (
    <section className="page-status">
      <p>{message}</p>
    </section>
  )
}

export function RequireAuth() {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <FullPageStatus message="Loading session..." />
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}

export function GuestOnly() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return <FullPageStatus message="Checking authentication..." />
  }

  if (isAuthenticated) {
    return <Navigate to="/manager-home" replace />
  }

  return <Outlet />
}
