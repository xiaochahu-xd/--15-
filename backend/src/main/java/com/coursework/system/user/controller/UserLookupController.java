package com.coursework.system.user.controller;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.user.dto.UserSummary;
import com.coursework.system.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserLookupController {
    private final UserService userService;

    public UserLookupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/selectable")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<List<UserSummary>> selectableUsers() {
        return ApiResponse.success(userService.listSummaries());
    }
}
