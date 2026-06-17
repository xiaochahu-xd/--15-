package com.coursework.system.common.security;

import com.coursework.system.common.response.ApiResponse;
import com.coursework.system.common.utils.RequestUtils;
import com.coursework.system.log.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final OperationLogService operationLogService;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, OperationLogService operationLogService) {
        this.objectMapper = objectMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            userId = principal.getId();
            username = principal.getUsername();
        }
        operationLogService.record(userId, username, "ACCESS_DENIED", "API", null,
                RequestUtils.getClientIp(request), "DENIED", request.getMethod() + " " + request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(403, "权限不足")));
    }
}
