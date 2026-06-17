import http from './http'
import type { ApiResponse, OperationLog, UserSummary } from '@/types'

export function listUsersApi() {
  return http.get<ApiResponse<UserSummary[]>>('/admin/users')
}

export function listOperationLogsApi(limit = 50) {
  return http.get<ApiResponse<OperationLog[]>>('/admin/operation-logs', {
    params: { limit }
  })
}
