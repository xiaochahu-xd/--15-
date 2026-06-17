package com.coursework.system.course.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CourseApplicationRejectRequest {
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因不能超过500个字符")
    private String rejectReason;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
