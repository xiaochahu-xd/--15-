import http from './http'
import type { ApiResponse, DuplicateRecord } from '@/types'

export function listDuplicateRecordsApi(assignmentId: number) {
  return http.get<ApiResponse<DuplicateRecord[]>>(`/assignments/${assignmentId}/duplicates`)
}

export function listSimilarityRecordsApi(assignmentId: number) {
  return http.get<ApiResponse<DuplicateRecord[]>>(`/assignments/${assignmentId}/similarity-records`)
}

export function listSubmissionSimilarityRecordsApi(submissionId: number) {
  return http.get<ApiResponse<DuplicateRecord[]>>(`/submissions/${submissionId}/similarity-records`)
}

export function confirmSimilarityRecordApi(id: number) {
  return http.put<ApiResponse<null>>(`/similarity-records/${id}/confirm`)
}

export function ignoreSimilarityRecordApi(id: number) {
  return http.put<ApiResponse<null>>(`/similarity-records/${id}/ignore`)
}
