import http from './http'
import type { ApiResponse, GradingDetail, GradingItem, GradingProgress } from '@/types'

export function listGradingListApi(assignmentId: number) {
  return http.get<ApiResponse<GradingItem[]>>(`/assignments/${assignmentId}/grading-list`)
}

export function getGradingSubmissionDetailApi(submissionId: number) {
  return http.get<ApiResponse<GradingDetail>>(`/submissions/${submissionId}/detail`)
}

export function saveGradeApi(submissionId: number, score: number, comment?: string) {
  return http.post<ApiResponse<GradingDetail>>(`/submissions/${submissionId}/grade`, { score, comment })
}

export function returnSubmissionApi(submissionId: number, reason?: string) {
  return http.put<ApiResponse<GradingDetail>>(`/submissions/${submissionId}/return`, { reason })
}

export function getSubmissionGradeApi(submissionId: number) {
  return http.get<ApiResponse<GradingDetail>>(`/submissions/${submissionId}/grade`)
}

export function getGradingProgressApi(courseId: number) {
  return http.get<ApiResponse<GradingProgress>>(`/courses/${courseId}/grading-progress`)
}
