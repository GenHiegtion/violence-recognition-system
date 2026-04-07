import axios, { AxiosError } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''
export const AUTH_STORAGE_KEY = 'vrs.auth'

interface StoredAuth {
  token: string
}

function readTokenFromStorage(): string | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw) as StoredAuth
    return parsed.token || null
  } catch {
    return null
  }
}

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = readTokenFromStorage()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

interface ErrorPayload {
  message?: string
  error?: string
}

export function getErrorMessage(error: unknown): string {
  if (!error) {
    return 'Unknown error'
  }

  if (error instanceof AxiosError) {
    const data = error.response?.data as ErrorPayload | undefined
    if (data?.message) {
      return data.message
    }
    if (data?.error) {
      return data.error
    }
    if (error.response?.status === 401) {
      return 'Unauthorized. Please login again.'
    }
    if (error.response?.status === 403) {
      return 'You do not have permission for this action.'
    }
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Unexpected error'
}
