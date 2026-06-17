import http from './http'
import type { ApiResponse, AuthResponse, CurrentUser, RoleCode } from '@/types'

export interface RegisterPayload {
  username: string
  password: string
  realName: string
  roleCode: Extract<RoleCode, 'TEACHER' | 'STUDENT'>
  email?: string
  phone?: string
}

export function loginApi(username: string, password: string) {
  return http.post<ApiResponse<AuthResponse>>('/auth/login', { username, password })
}

export function registerApi(payload: RegisterPayload) {
  return http.post<ApiResponse<AuthResponse>>('/auth/register', payload)
}

export function meApi() {
  return http.get<ApiResponse<CurrentUser>>('/auth/me')
}
