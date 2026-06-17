import http from './http'
import type { ApiResponse, UserSummary } from '@/types'

export function listSelectableUsersApi() {
  return http.get<ApiResponse<UserSummary[]>>('/users/selectable')
}
