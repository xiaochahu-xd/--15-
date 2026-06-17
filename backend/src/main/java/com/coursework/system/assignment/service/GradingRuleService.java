package com.coursework.system.assignment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.assignment.entity.GradingRule;

public interface GradingRuleService extends IService<GradingRule> {
    void createDefaultRules(Long assignmentId, String assignmentType, boolean allowLate);
}
