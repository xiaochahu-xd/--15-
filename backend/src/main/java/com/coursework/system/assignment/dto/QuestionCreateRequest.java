package com.coursework.system.assignment.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class QuestionCreateRequest {
    @NotBlank(message = "题目类型不能为空")
    private String questionType;
    @NotBlank(message = "题干不能为空")
    @Size(max = 2000, message = "题干不能超过2000个字符")
    private String content;
    @Size(max = 2000, message = "选项不能超过2000个字符")
    private String options;
    @NotBlank(message = "标准答案不能为空")
    @Size(max = 500, message = "标准答案不能超过500个字符")
    private String standardAnswer;
    @NotNull(message = "题目分值不能为空")
    @DecimalMin(value = "0.1", message = "题目分值必须大于0")
    private BigDecimal score;

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
