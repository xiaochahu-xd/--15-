package com.coursework.system.grading.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class GradeRequest {
    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.0", message = "分数不能小于0")
    private BigDecimal score;

    @Size(max = 1000, message = "评语不能超过1000个字符")
    private String comment;

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
