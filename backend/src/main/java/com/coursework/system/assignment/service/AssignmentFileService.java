package com.coursework.system.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.assignment.dto.AssignmentFileSummary;
import com.coursework.system.assignment.entity.AssignmentFile;
import com.coursework.system.common.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssignmentFileService extends IService<AssignmentFile> {
    AssignmentFileSummary upload(Long assignmentId, MultipartFile file, UserPrincipal principal, String ip);

    List<AssignmentFileSummary> listByAssignment(Long assignmentId, UserPrincipal principal);

    AssignmentFile getForDownload(Long fileId, UserPrincipal principal);
}
