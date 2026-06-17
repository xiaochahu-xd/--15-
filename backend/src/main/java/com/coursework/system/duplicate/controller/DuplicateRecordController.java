package com.coursework.system.duplicate.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.duplicate.dto.DuplicateRecordSummary;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DuplicateRecordController {
    private final DuplicateRecordService duplicateRecordService;

    public DuplicateRecordController(DuplicateRecordService duplicateRecordService) {
        this.duplicateRecordService = duplicateRecordService;
    }

    @GetMapping("/api/assignments/{assignmentId}/duplicates")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<DuplicateRecordSummary>> listByAssignment(@PathVariable Long assignmentId,
                                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(duplicateRecordService.listByAssignmentForReviewer(assignmentId, principal));
    }

    @GetMapping("/api/assignments/{assignmentId}/similarity-records")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<DuplicateRecordSummary>> listSimilarityByAssignment(@PathVariable Long assignmentId,
                                                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(duplicateRecordService.listByAssignmentForReviewer(assignmentId, principal));
    }

    @GetMapping("/api/submissions/{submissionId}/similarity-records")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<DuplicateRecordSummary>> listSimilarityBySubmission(@PathVariable Long submissionId,
                                                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(duplicateRecordService.listBySubmissionForReviewer(submissionId, principal));
    }

    @PutMapping("/api/similarity-records/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<Void> confirm(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        duplicateRecordService.confirm(id, principal);
        return ApiResponse.success("相似度核查记录已确认", null);
    }

    @PutMapping("/api/similarity-records/{id}/ignore")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<Void> ignore(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        duplicateRecordService.ignore(id, principal);
        return ApiResponse.success("相似度核查记录已忽略", null);
    }
}
