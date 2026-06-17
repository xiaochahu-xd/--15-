package com.coursework.system.assignment.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AssignmentCreateRequest {
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 120, message = "作业标题不能超过120个字符")
    private String title;
    @Size(max = 2000, message = "作业说明不能超过2000个字符")
    private String description;
    @NotBlank(message = "作业类型不能为空")
    private String assignmentType;
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    @NotNull(message = "满分不能为空")
    @DecimalMin(value = "0.1", message = "满分必须大于0")
    private BigDecimal totalScore;
    private Boolean allowLate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Boolean getAllowLate() {
        return allowLate;
    }

    public void setAllowLate(Boolean allowLate) {
        this.allowLate = allowLate;
    }
}
