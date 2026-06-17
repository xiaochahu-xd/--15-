package com.coursework.system.submission.dto;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

public class ObjectiveSubmissionRequest {
    @NotEmpty(message = "答案不能为空")
    private Map<String, String> answers;

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}
