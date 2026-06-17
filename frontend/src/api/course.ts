import http from './http'
import type { ApiResponse, Course, CourseApplication, CourseDetailSummary, CourseMember } from '@/types'

export function createCourseApplicationApi(payload: {
  courseName: string
  courseCode: string
  description?: string
}) {
  return http.post<ApiResponse<CourseApplication>>('/course-applications', payload)
}

export function listPendingCourseApplicationsApi() {
  return http.get<ApiResponse<CourseApplication[]>>('/course-applications/pending')
}

export function approveCourseApplicationApi(id: number) {
  return http.put<ApiResponse<CourseApplication>>(`/course-applications/${id}/approve`)
}

export function rejectCourseApplicationApi(id: number, rejectReason: string) {
  return http.put<ApiResponse<CourseApplication>>(`/course-applications/${id}/reject`, { rejectReason })
}

export function listMyCoursesApi() {
  return http.get<ApiResponse<Course[]>>('/courses/my')
}

export function listAllCoursesApi() {
  return http.get<ApiResponse<Course[]>>('/courses')
}

export function listCourseMembersApi(courseId: number) {
  return http.get<ApiResponse<CourseMember[]>>(`/courses/${courseId}/members`)
}

export function getCourseDetailApi(courseId: number) {
  return http.get<ApiResponse<CourseDetailSummary>>(`/courses/${courseId}/detail`)
}

export function addCourseMemberApi(courseId: number, payload: { userId: number; memberRole: string }) {
  return http.post<ApiResponse<CourseMember>>(`/courses/${courseId}/members`, payload)
}

export function removeCourseMemberApi(courseId: number, userId: number) {
  return http.delete<ApiResponse<null>>(`/courses/${courseId}/members/${userId}`)
}
