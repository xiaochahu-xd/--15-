import http from './http'
import type { ApiResponse, AssignmentStatistics, CourseStatistics, GradeExportRow } from '@/types'

export function getCourseStatisticsApi(courseId: number) {
  return http.get<ApiResponse<CourseStatistics>>(`/courses/${courseId}/statistics`)
}

export function getAssignmentStatisticsApi(assignmentId: number) {
  return http.get<ApiResponse<AssignmentStatistics>>(`/assignments/${assignmentId}/statistics`)
}

export function listCourseGradesApi(courseId: number) {
  return http.get<ApiResponse<GradeExportRow[]>>(`/courses/${courseId}/grades`)
}

export function exportCourseGradesApi(courseId: number) {
  return http.get<Blob>(`/courses/${courseId}/grades/export`, { responseType: 'blob' })
}
