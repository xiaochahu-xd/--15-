package com.coursework.system.rule;

import org.springframework.stereotype.Component;

@Component
public class LateSubmitRule implements Rule {

    @Override
    public String getName() {
        return "LateSubmitRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null
                && context.getSubmission() != null
                && context.getAssignment().getDeadline() != null
                && context.getSubmission().getSubmitTime() != null;
    }

    @Override
    public RuleResult execute(RuleContext context) {
        RuleResult result = new RuleResult();
        boolean late = context.getSubmission().getSubmitTime().isAfter(context.getAssignment().getDeadline());
        result.setLate(late);
        result.addMessage(late ? "提交时间晚于截止时间，标记为迟交" : "提交时间未超过截止时间");
        return result;
    }
}
