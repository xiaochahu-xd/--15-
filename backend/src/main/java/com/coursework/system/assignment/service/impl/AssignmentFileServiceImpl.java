package com.coursework.system.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.dto.AssignmentFileSummary;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.AssignmentFile;
import com.coursework.system.assignment.mapper.AssignmentFileMapper;
import com.coursework.system.assignment.service.AssignmentFileService;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.submission.config.FileStorageProperties;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentFileServiceImpl extends ServiceImpl<AssignmentFileMapper, AssignmentFile> implements AssignmentFileService {
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AssignmentService assignmentService;
    private final FileStorageProperties fileStorageProperties;
    private final UserService userService;
    private final OperationLogService operationLogService;

    public AssignmentFileServiceImpl(AssignmentService assignmentService,
                                     FileStorageProperties fileStorageProperties,
                                     UserService userService,
                                     OperationLogService operationLogService) {
        this.assignmentService = assignmentService;
        this.fileStorageProperties = fileStorageProperties;
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

    @Override
    public AssignmentFileSummary upload(Long assignmentId, MultipartFile file, UserPrincipal principal, String ip) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!assignmentService.canEditAssignment(assignment, principal)) {
            recordDenied(principal, ip, assignmentId, "无权上传作业附件");
            throw new BusinessException(403, "只有课程负责人或课程教师可以上传作业附件");
        }
        if (!"FILE".equals(assignment.getAssignmentType())) {
            throw new BusinessException(400, "只有文件作业可以上传作业附件");
        }
        try {
            Path root = Paths.get(fileStorageProperties.getRoot()).toAbsolutePath().normalize();
            Path folder = root.resolve("assignment-materials")
                    .resolve("assignment-" + assignmentId)
                    .resolve(LocalDate.now().toString())
                    .normalize();
            Files.createDirectories(folder);

            String originalName = file.getOriginalFilename() == null ? "assignment-file.bin" : file.getOriginalFilename();
            String safeName = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + safeName;
            Path target = folder.resolve(storedName).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException(400, "文件路径非法");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = file.getInputStream();
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                Files.copy(digestInputStream, target);
            }

            AssignmentFile record = new AssignmentFile();
            record.setAssignmentId(assignmentId);
            record.setFileName(originalName);
            record.setFilePath(target.toString());
            record.setFileSize(Files.size(target));
            record.setFileHash(toHex(digest.digest()));
            record.setUploadedBy(principal.getId());
            record.setUploadedAt(LocalDateTime.now());
            record.setStatus(STATUS_ACTIVE);
            save(record);
            operationLogService.record(principal.getId(), principal.getUsername(), "ASSIGNMENT_FILE_UPLOAD",
                    "ASSIGNMENT", assignmentId, ip, "SUCCESS", "上传作业附件：" + originalName);
            return toSummary(record);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(500, "作业附件保存失败：" + exception.getMessage());
        }
    }

    @Override
    public List<AssignmentFileSummary> listByAssignment(Long assignmentId, UserPrincipal principal) {
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        if (!assignmentService.canViewAssignment(assignment, principal)) {
            throw new BusinessException(403, "无权查看该作业附件");
        }
        List<AssignmentFile> files = list(new QueryWrapper<AssignmentFile>()
                .eq("assignment_id", assignmentId)
                .eq("status", STATUS_ACTIVE)
                .orderByDesc("uploaded_at"));
        List<AssignmentFileSummary> summaries = new ArrayList<AssignmentFileSummary>();
        for (AssignmentFile file : files) {
            summaries.add(toSummary(file));
        }
        return summaries;
    }

    @Override
    public AssignmentFile getForDownload(Long fileId, UserPrincipal principal) {
        AssignmentFile file = getById(fileId);
        if (file == null || !STATUS_ACTIVE.equals(file.getStatus())) {
            throw new BusinessException(404, "作业附件不存在");
        }
        Assignment assignment = assignmentService.getActiveAssignment(file.getAssignmentId());
        if (!assignmentService.canViewAssignment(assignment, principal)) {
            throw new BusinessException(403, "无权下载该作业附件");
        }
        return file;
    }

    private AssignmentFileSummary toSummary(AssignmentFile file) {
        AssignmentFileSummary summary = new AssignmentFileSummary();
        summary.setId(file.getId());
        summary.setAssignmentId(file.getAssignmentId());
        summary.setFileName(file.getFileName());
        summary.setFileSize(file.getFileSize());
        summary.setFileHash(file.getFileHash());
        summary.setUploadedBy(file.getUploadedBy());
        User uploader = userService.getById(file.getUploadedBy());
        summary.setUploadedByName(uploader == null ? "" : uploader.getRealName());
        summary.setUploadedAt(file.getUploadedAt());
        return summary;
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private void recordDenied(UserPrincipal principal, String ip, Long assignmentId, String detail) {
        if (principal != null) {
            operationLogService.record(principal.getId(), principal.getUsername(), "ASSIGNMENT_FILE_UPLOAD",
                    "ASSIGNMENT", assignmentId, ip, "DENIED", detail);
        }
    }
}
