import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { GuestOnly, RequireAdmin, RequireAuth } from './components/RouteGuards'
import { useAuth } from './context/useAuth'
import { LoginPage } from './pages/LoginPage'
import { ManagerHomePage } from './pages/ManagerHomePage'
import { PatternDetailPage } from './pages/PatternDetailPage'
import { PatternFormPage } from './pages/PatternFormPage'
import { PatternManagementPage } from './pages/PatternManagementPage'
import { RecognitionModelSelectionPage } from './pages/RecognitionModelSelectionPage'
import { RecognitionVideoSelectionPage } from './pages/RecognitionVideoSelectionPage'
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
          <Route path="/recognition" element={<Navigate to="/recognition/video" replace />} />
          <Route path="/recognition/video" element={<RecognitionVideoSelectionPage />} />
          <Route path="/recognition/models" element={<RecognitionModelSelectionPage />} />
          <Route path="/recognition/result" element={<ViolenceRecognitionPage />} />
        </Route>

        <Route element={<RequireAdmin />}>
          <Route path="/patterns" element={<PatternManagementPage />} />
          <Route path="/patterns/add" element={<PatternFormPage />} />
          <Route path="/patterns/:id" element={<PatternDetailPage />} />
          <Route path="/patterns/:id/edit" element={<PatternFormPage />} />
          <Route path="/statistics" element={<StatisticsPage />} />
        </Route>

        <Route path="*" element={<HomeRedirect />} />
      </Route>
    </Routes>
  )
}

export default App
