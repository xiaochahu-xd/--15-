package com.coursework.system.course.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CourseMemberAddRequest {
    @NotNull(message = "用户不能为空")
    private Long userId;
    @NotBlank(message = "成员角色不能为空")
    private String memberRole;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }
}
