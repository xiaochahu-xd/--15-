import http from './http'
import type { ApiResponse, StudentSubmissionAggregate, Submission, SubmissionDetail } from '@/types'

export function submitTextApi(assignmentId: number, content: string) {
  return http.post<ApiResponse<SubmissionDetail>>(`/assignments/${assignmentId}/submissions/text`, { content })
}

export function submitFileApi(assignmentId: number, file: File, content?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (content) {
    formData.append('content', content)
  }
  return http.post<ApiResponse<SubmissionDetail>>(`/assignments/${assignmentId}/submissions/file`, formData)
}

export function submitObjectiveApi(assignmentId: number, answers: Record<string, string>) {
  return http.post<ApiResponse<SubmissionDetail>>(`/assignments/${assignmentId}/submissions/objective`, { answers })
}

export function listMySubmissionsApi(assignmentId: number) {
  return http.get<ApiResponse<Submission[]>>(`/assignments/${assignmentId}/submissions/my`)
}

export function listAssignmentSubmissionsApi(assignmentId: number) {
  return http.get<ApiResponse<Submission[]>>(`/assignments/${assignmentId}/submissions`)
}

export function listStudentSubmissionsApi(params?: {
  courseId?: number
  assignmentType?: string
  status?: string
  keyword?: string
  page?: number
  size?: number
}) {
  return http.get<ApiResponse<StudentSubmissionAggregate[]>>('/student/submissions', { params })
}

export function getSubmissionApi(submissionId: number) {
  return http.get<ApiResponse<SubmissionDetail>>(`/submissions/${submissionId}`)
}

export function downloadFileRecordApi(fileRecordId: number) {
  return http.get<Blob>(`/file-records/${fileRecordId}/download`, { responseType: 'blob' })
}
