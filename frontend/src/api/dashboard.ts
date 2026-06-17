import http from './http'
import type { ApiResponse, DashboardData } from '@/types'

export function getDashboardApi() {
  return http.get<ApiResponse<DashboardData>>('/dashboard')
}
