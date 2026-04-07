import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../services/authApi'
import { AUTH_STORAGE_KEY } from '../services/http'
import { userApi } from '../services/userApi'
import type { LoginRequest, RegisterRequest, UserResponse } from '../types/auth'
import { AuthContext } from './auth-context'
import type { AuthContextValue, AuthState } from './auth-context'

interface StoredAuth {
  token: string
  user: UserResponse
}

function readStoredAuth(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw) as StoredAuth
    if (!parsed.token || !parsed.user) {
      return null
    }
    return parsed
  } catch {
    return null
  }
}

function persistAuth(token: string, user: UserResponse): void {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({ token, user }))
}

function clearAuthStorage(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    token: null,
    user: null,
    isLoading: true,
  })

  useEffect(() => {
    let cancelled = false

    const initialize = async () => {
      const stored = readStoredAuth()
      if (!stored) {
        if (!cancelled) {
          setState({ token: null, user: null, isLoading: false })
        }
        return
      }

      if (!cancelled) {
        setState({ token: stored.token, user: stored.user, isLoading: true })
      }

      try {
        const currentUser = await userApi.me()
        if (!cancelled) {
          persistAuth(stored.token, currentUser)
          setState({ token: stored.token, user: currentUser, isLoading: false })
        }
      } catch {
        clearAuthStorage()
        if (!cancelled) {
          setState({ token: null, user: null, isLoading: false })
        }
      }
    }

    void initialize()

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await authApi.login(payload)
    persistAuth(response.token, response.user)
    setState({ token: response.token, user: response.user, isLoading: false })
  }, [])

  const register = useCallback(async (payload: RegisterRequest) => {
    return authApi.register(payload)
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // Logout should still clear local session even if backend revocation fails.
    } finally {
      clearAuthStorage()
      setState({ token: null, user: null, isLoading: false })
    }
  }, [])

  const refreshProfile = useCallback(async () => {
    if (!state.token) {
      return
    }

    const profile = await userApi.me()
    persistAuth(state.token, profile)
    setState((prev) => ({ ...prev, user: profile }))
  }, [state.token])

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      isAuthenticated: Boolean(state.token && state.user),
      isAdmin: state.user?.role === 'ADMIN',
      login,
      register,
      logout,
      refreshProfile,
    }),
    [login, logout, refreshProfile, register, state],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
