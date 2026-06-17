package com.coursework.system.common.security;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.log.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final OperationLogService operationLogService;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper, OperationLogService operationLogService) {
        this.objectMapper = objectMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        operationLogService.record(null, null, "AUTH_REQUIRED", "API", null,
                RequestUtils.getClientIp(request), "DENIED", request.getMethod() + " " + request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(401, "请先登录")));
    }
}
