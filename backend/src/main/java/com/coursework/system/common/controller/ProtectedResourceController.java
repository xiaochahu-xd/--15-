package com.coursework.system.common.controller;

import com.coursework.system.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ProtectedResourceController {

    @GetMapping("/api/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> adminPing() {
        return message("管理员接口访问成功");
    }

    @GetMapping("/api/teacher/ping")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Map<String, String>> teacherPing() {
        return message("教师接口访问成功");
    }

    @GetMapping("/api/assistant/ping")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','ASSISTANT')")
    public ApiResponse<Map<String, String>> assistantPing() {
        return message("助教接口访问成功");
    }

    @GetMapping("/api/student/ping")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ApiResponse<Map<String, String>> studentPing() {
        return message("学生接口访问成功");
    }

    private ApiResponse<Map<String, String>> message(String text) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("message", text);
        return ApiResponse.success(result);
    }
}
