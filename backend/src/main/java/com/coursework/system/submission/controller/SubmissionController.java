package com.coursework.system.submission.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.submission.dto.ObjectiveSubmissionRequest;
import com.coursework.system.submission.dto.StudentSubmissionAggregate;
import com.coursework.system.submission.dto.SubmissionDetail;
import com.coursework.system.submission.dto.SubmissionSummary;
import com.coursework.system.submission.dto.TextSubmissionRequest;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.submission.service.SubmissionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class SubmissionController {
    private final SubmissionService submissionService;
    private final FileRecordService fileRecordService;

    public SubmissionController(SubmissionService submissionService, FileRecordService fileRecordService) {
        this.submissionService = submissionService;
        this.fileRecordService = fileRecordService;
    }

    @PostMapping("/api/assignments/{assignmentId}/submissions/text")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<SubmissionDetail> submitText(@PathVariable Long assignmentId,
                                                    @Valid @RequestBody TextSubmissionRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest servletRequest) {
        return ApiResponse.success("文本作业已提交",
                submissionService.submitText(assignmentId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @PostMapping("/api/assignments/{assignmentId}/submissions/file")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<SubmissionDetail> submitFile(@PathVariable Long assignmentId,
                                                    @RequestPart("file") MultipartFile file,
                                                    @RequestParam(value = "content", required = false) String content,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest servletRequest) {
        return ApiResponse.success("文件作业已提交",
                submissionService.submitFile(assignmentId, file, content, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @PostMapping("/api/assignments/{assignmentId}/submissions/objective")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<SubmissionDetail> submitObjective(@PathVariable Long assignmentId,
                                                         @Valid @RequestBody ObjectiveSubmissionRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         HttpServletRequest servletRequest) {
        return ApiResponse.success("客观题答案已提交",
                submissionService.submitObjective(assignmentId, request, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping("/api/assignments/{assignmentId}/submissions/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<SubmissionSummary>> mySubmissions(@PathVariable Long assignmentId,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(submissionService.listMySubmissions(assignmentId, principal));
    }

    @GetMapping("/api/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<List<SubmissionSummary>> assignmentSubmissions(@PathVariable Long assignmentId,
                                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(submissionService.listAssignmentSubmissions(assignmentId, principal));
    }

    @GetMapping("/api/student/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<StudentSubmissionAggregate>> studentSubmissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "assignmentType", required = false) String assignmentType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return ApiResponse.success(submissionService.listStudentSubmissions(
                principal, courseId, assignmentType, status, keyword, page, size));
    }

    @GetMapping("/api/submissions/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SubmissionDetail> detail(@PathVariable Long submissionId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(submissionService.getSubmissionDetail(submissionId, principal));
    }

    @GetMapping("/api/file-records/{fileRecordId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long fileRecordId,
                                             @AuthenticationPrincipal UserPrincipal principal) throws Exception {
        FileRecord fileRecord = fileRecordService.getById(fileRecordId);
        if (fileRecord == null) {
            throw new com.coursework.system.common.exception.BusinessException(404, "文件记录不存在");
        }
        submissionService.getSubmissionDetail(fileRecord.getSubmissionId(), principal);
        Path path = Paths.get(fileRecord.getFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new com.coursework.system.common.exception.BusinessException(404, "文件不存在");
        }
        Resource resource = new UrlResource(path.toUri());
        String encodedName = URLEncoder.encode(fileRecord.getFileName(), StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(Files.size(path))
                .body(resource);
    }
}
