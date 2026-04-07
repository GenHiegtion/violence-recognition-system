import { http } from './http'
import type {
  AuthResponse,
  LoginRequest,
  LogoutResponse,
  RegisterRequest,
  UserResponse,
} from '../types/auth'

export const authApi = {
  async register(payload: RegisterRequest): Promise<UserResponse> {
    const { data } = await http.post<UserResponse>('/api/auth/register', payload)
    return data
  },

  async login(payload: LoginRequest): Promise<AuthResponse> {
    const { data } = await http.post<AuthResponse>('/api/auth/login', payload)
    return data
  },

  async logout(): Promise<LogoutResponse> {
    const { data } = await http.post<LogoutResponse>('/api/auth/logout')
    return data
  },
}
