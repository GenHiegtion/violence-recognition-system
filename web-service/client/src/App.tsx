import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { GuestOnly, RequireAuth } from './components/RouteGuards'
import { useAuth } from './context/useAuth'
import { LoginPage } from './pages/LoginPage'
import { ManagerHomePage } from './pages/ManagerHomePage'
import { PatternDetailPage } from './pages/PatternDetailPage'
import { PatternFormPage } from './pages/PatternFormPage'
import { PatternManagementPage } from './pages/PatternManagementPage'
import { RegisterPage } from './pages/RegisterPage'
import { StatisticsPage } from './pages/StatisticsPage'
import { ViolenceRecognitionPage } from './pages/ViolenceRecognitionPage'

function HomeRedirect() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <section className="page-status">
        <p>Loading dashboard...</p>
      </section>
    )
  }

  return <Navigate to={isAuthenticated ? '/manager-home' : '/login'} replace />
}

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomeRedirect />} />

        <Route element={<GuestOnly />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<RequireAuth />}>
          <Route path="/manager-home" element={<ManagerHomePage />} />
          <Route path="/patterns" element={<PatternManagementPage />} />
          <Route path="/patterns/add" element={<PatternFormPage />} />
          <Route path="/patterns/:id" element={<PatternDetailPage />} />
          <Route path="/patterns/:id/edit" element={<PatternFormPage />} />
          <Route path="/recognition" element={<ViolenceRecognitionPage />} />
          <Route path="/statistics" element={<StatisticsPage />} />
        </Route>

        <Route path="*" element={<HomeRedirect />} />
      </Route>
    </Routes>
  )
}

export default App
