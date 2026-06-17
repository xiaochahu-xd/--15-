package com.coursework.system.assignment.controller;

import com.coursework.system.assignment.dto.AssignmentFileSummary;
import com.coursework.system.assignment.entity.AssignmentFile;
import com.coursework.system.assignment.service.AssignmentFileService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.common.utils.RequestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class AssignmentFileController {
    private final AssignmentFileService assignmentFileService;

    public AssignmentFileController(AssignmentFileService assignmentFileService) {
        this.assignmentFileService = assignmentFileService;
    }

    @PostMapping("/api/assignments/{assignmentId}/files")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<AssignmentFileSummary> upload(@PathVariable Long assignmentId,
                                                     @RequestPart("file") MultipartFile file,
                                                     @AuthenticationPrincipal UserPrincipal principal,
                                                     HttpServletRequest servletRequest) {
        return ApiResponse.success("作业附件已上传",
                assignmentFileService.upload(assignmentId, file, principal, RequestUtils.getClientIp(servletRequest)));
    }

    @GetMapping("/api/assignments/{assignmentId}/files")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AssignmentFileSummary>> list(@PathVariable Long assignmentId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(assignmentFileService.listByAssignment(assignmentId, principal));
    }

    @GetMapping("/api/assignment-files/{fileId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long fileId,
                                             @AuthenticationPrincipal UserPrincipal principal) throws Exception {
        AssignmentFile file = assignmentFileService.getForDownload(fileId, principal);
        Path path = Paths.get(file.getFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BusinessException(404, "作业附件文件不存在");
        }
        Resource resource = new UrlResource(path.toUri());
        String encodedName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(Files.size(path))
                .body(resource);
    }
}
