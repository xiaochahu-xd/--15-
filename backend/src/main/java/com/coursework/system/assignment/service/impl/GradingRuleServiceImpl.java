package com.coursework.system.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.assignment.entity.GradingRule;
import com.coursework.system.assignment.mapper.GradingRuleMapper;
import com.coursework.system.assignment.service.GradingRuleService;
import org.springframework.stereotype.Service;

@Service
public class GradingRuleServiceImpl extends ServiceImpl<GradingRuleMapper, GradingRule> implements GradingRuleService {

    @Override
    public void createDefaultRules(Long assignmentId, String assignmentType, boolean allowLate) {
        ensureRule(assignmentId, "LATE_SUBMISSION",
                "submitted_at > deadline",
                allowLate ? "mark_late_without_reject" : "reject_or_mark_late");
        if ("SINGLE_CHOICE".equals(assignmentType) || "TRUE_FALSE".equals(assignmentType)) {
            ensureRule(assignmentId, "OBJECTIVE_AUTO_SCORE",
                    "answer == standard_answer",
                    "award_question_score");
        }
    }

    private void ensureRule(Long assignmentId, String ruleType, String conditionExpr, String actionExpr) {
        long existing = count(new QueryWrapper<GradingRule>()
                .eq("assignment_id", assignmentId)
                .eq("rule_type", ruleType));
        if (existing > 0) {
            return;
        }
        GradingRule rule = new GradingRule();
        rule.setAssignmentId(assignmentId);
        rule.setRuleType(ruleType);
        rule.setConditionExpr(conditionExpr);
        rule.setActionExpr(actionExpr);
        rule.setEnabled(1);
        save(rule);
    }
}
