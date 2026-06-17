package com.coursework.system.rule;

public interface Rule {
    String getName();

    boolean supports(RuleContext context);

    RuleResult execute(RuleContext context);
}
