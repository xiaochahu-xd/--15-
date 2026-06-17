package com.coursework.system.submission.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TextSubmissionRequest {
    @NotBlank(message = "提交内容不能为空")
    @Size(max = 10000, message = "提交内容不能超过10000个字符")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
