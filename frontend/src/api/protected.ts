import http from './http'
import type { ApiResponse } from '@/types'

export function pingApi(rolePath: 'admin' | 'teacher' | 'assistant' | 'student') {
  return http.get<ApiResponse<{ message: string }>>(`/${rolePath}/ping`)
}
