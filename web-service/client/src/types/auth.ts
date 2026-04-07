export type UserRole = 'ADMIN' | 'MEMBER'

export interface UserResponse {
  id: number
  username: string
  fullName: string
  role: UserRole
  createdAt: string
}

export interface RegisterRequest {
  username: string
  fullName: string
  password: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  issuedAt: string
  user: UserResponse
}

export interface LogoutResponse {
  loggedOut: boolean
}
