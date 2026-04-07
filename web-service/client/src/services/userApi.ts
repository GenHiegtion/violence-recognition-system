import { http } from './http'
import type { UserResponse } from '../types/auth'

export const userApi = {
  async me(): Promise<UserResponse> {
    const { data } = await http.get<UserResponse>('/api/users/me')
    return data
  },

  async list(): Promise<UserResponse[]> {
    const { data } = await http.get<UserResponse[]>('/api/users')
    return data
  },
}
