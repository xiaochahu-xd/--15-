import http from './http'
import type { ApiResponse, Assignment, AssignmentFile, AssignmentType, Question } from '@/types'

export interface AssignmentPayload {
  title: string
  description?: string
  assignmentType: AssignmentType
  deadline: string
  totalScore: number
  allowLate: boolean
}

export function createAssignmentApi(courseId: number, payload: AssignmentPayload) {
  return http.post<ApiResponse<Assignment>>(`/courses/${courseId}/assignments`, payload)
}

export function listAssignmentsApi(courseId: number) {
  return http.get<ApiResponse<Assignment[]>>(`/courses/${courseId}/assignments`)
}

export function getAssignmentApi(assignmentId: number) {
  return http.get<ApiResponse<Assignment>>(`/assignments/${assignmentId}`)
}

export function updateAssignmentApi(assignmentId: number, payload: AssignmentPayload) {
  return http.put<ApiResponse<Assignment>>(`/assignments/${assignmentId}`, payload)
}

export function deleteAssignmentApi(assignmentId: number) {
  return http.delete<ApiResponse<null>>(`/assignments/${assignmentId}`)
}

export function uploadAssignmentFileApi(assignmentId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResponse<AssignmentFile>>(`/assignments/${assignmentId}/files`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listAssignmentFilesApi(assignmentId: number) {
  return http.get<ApiResponse<AssignmentFile[]>>(`/assignments/${assignmentId}/files`)
}

export function downloadAssignmentFileApi(fileId: number) {
  return http.get<Blob>(`/assignment-files/${fileId}/download`, { responseType: 'blob' })
}

export function addQuestionApi(assignmentId: number, payload: {
  questionType: 'SINGLE_CHOICE' | 'TRUE_FALSE'
  content: string
  options?: string
  standardAnswer: string
  score: number
}) {
  return http.post<ApiResponse<Question>>(`/assignments/${assignmentId}/questions`, payload)
}

export function listQuestionsApi(assignmentId: number) {
  return http.get<ApiResponse<Question[]>>(`/assignments/${assignmentId}/questions`)
}
