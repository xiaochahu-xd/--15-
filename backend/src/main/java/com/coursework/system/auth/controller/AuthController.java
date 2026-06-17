package com.coursework.system.auth.controller;

import com.coursework.system.auth.dto.AuthResponse;
import com.coursework.system.auth.dto.CurrentUserResponse;
import com.coursework.system.auth.dto.LoginRequest;
import com.coursework.system.auth.dto.RegisterRequest;
import com.coursework.system.auth.service.AuthService;
import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return ApiResponse.success(authService.login(loginRequest, request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                              HttpServletRequest request) {
        return ApiResponse.success("注册成功", authService.register(registerRequest, request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(authService.toCurrentUser(principal));
    }
}
