package com.coursework.system.rule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GradeLevelRule implements Rule {

    @Override
    public String getName() {
        return "GradeLevelRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null && context.getSubmission() != null
                && context.getAssignment().getTotalScore() != null
                && context.getSubmission().getAutoScore() != null;
    }

    @Override
    public RuleResult execute(RuleContext context) {
        RuleResult result = new RuleResult();
        BigDecimal totalScore = context.getAssignment().getTotalScore();
        if (totalScore.signum() <= 0) {
            return result;
        }
        BigDecimal ratio = context.getSubmission().getAutoScore()
                .multiply(new BigDecimal("100"))
                .divide(totalScore, 2, BigDecimal.ROUND_HALF_UP);
        if (ratio.compareTo(new BigDecimal("90")) >= 0) {
            result.setGradeLevel("A");
        } else if (ratio.compareTo(new BigDecimal("80")) >= 0) {
            result.setGradeLevel("B");
        } else if (ratio.compareTo(new BigDecimal("70")) >= 0) {
            result.setGradeLevel("C");
        } else if (ratio.compareTo(new BigDecimal("60")) >= 0) {
            result.setGradeLevel("D");
        } else {
            result.setGradeLevel("F");
        }
        result.addMessage("成绩等级预计算：" + result.getGradeLevel());
        return result;
    }
}
