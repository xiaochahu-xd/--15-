package com.coursework.system.log.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.log.entity.OperationLog;
import com.coursework.system.log.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {
    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OperationLog>> latest(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(operationLogService.latest(limit));
    }
}
