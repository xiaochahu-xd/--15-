import http from './http'
import type { ApiResponse, Notification } from '@/types'

export function listNotificationsApi(courseId?: number) {
  return http.get<ApiResponse<Notification[]>>('/notifications', { params: { courseId } })
}

export function listCourseNotificationsApi(courseId: number) {
  return http.get<ApiResponse<Notification[]>>(`/courses/${courseId}/notifications`)
}

export function markNotificationReadApi(id: number) {
  return http.put<ApiResponse<null>>(`/notifications/${id}/read`)
}

export function markAllNotificationsReadApi() {
  return http.put<ApiResponse<null>>('/notifications/read-all')
}

export function getUnreadNotificationCountApi() {
  return http.get<ApiResponse<number>>('/notifications/unread-count')
}
