import http from './http'
import type { ApiResponse, AuthResponse, CurrentUser } from '@/types'

export function loginApi(username: string, password: string) {
  return http.post<ApiResponse<AuthResponse>>('/auth/login', { username, password })
}

export function meApi() {
  return http.get<ApiResponse<CurrentUser>>('/auth/me')
}
