package com.coursework.system.grading.dto;

import javax.validation.constraints.Size;

public class ReturnSubmissionRequest {
    @Size(max = 1000, message = "退回原因不能超过1000个字符")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
