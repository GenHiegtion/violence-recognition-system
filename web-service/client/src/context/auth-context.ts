import { createContext } from 'react'
import type { LoginRequest, RegisterRequest, UserResponse } from '../types/auth'

export interface AuthState {
  token: string | null
  user: UserResponse | null
  isLoading: boolean
}

export interface AuthContextValue extends AuthState {
  isAuthenticated: boolean
  isAdmin: boolean
  login: (payload: LoginRequest) => Promise<UserResponse>
  register: (payload: RegisterRequest) => Promise<UserResponse>
  logout: () => Promise<void>
  refreshProfile: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
