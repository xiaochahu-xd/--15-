package com.coursework.system.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("grading_rules")
public class GradingRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("assignment_id")
    private Long assignmentId;
    @TableField("rule_type")
    private String ruleType;
    @TableField("condition_expr")
    private String conditionExpr;
    @TableField("action_expr")
    private String actionExpr;
    private Integer enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getConditionExpr() {
        return conditionExpr;
    }

    public void setConditionExpr(String conditionExpr) {
        this.conditionExpr = conditionExpr;
    }

    public String getActionExpr() {
        return actionExpr;
    }

    public void setActionExpr(String actionExpr) {
        this.actionExpr = actionExpr;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
