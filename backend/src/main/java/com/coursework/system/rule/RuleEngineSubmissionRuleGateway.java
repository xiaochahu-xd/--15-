package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.submission.entity.Submission;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RuleEngineSubmissionRuleGateway implements SubmissionRuleGateway {
    private final RuleEngine ruleEngine;
    private final OperationLogService operationLogService;

    public RuleEngineSubmissionRuleGateway(RuleEngine ruleEngine, OperationLogService operationLogService) {
        this.ruleEngine = ruleEngine;
        this.operationLogService = operationLogService;
    }

    @Override
    public void afterSubmissionCreated(Assignment assignment, Submission submission, UserPrincipal principal, String ip) {
        try {
            RuleResult result = ruleEngine.evaluateAndApply(assignment, submission);
            operationLogService.record(principal.getId(), principal.getUsername(), "RULE_ENGINE_EXECUTE",
                    "SUBMISSION", submission.getId(), ip, "SUCCESS", String.join("；", result.getMessages()));
        } catch (BusinessException exception) {
            recordFailure(submission, principal, ip, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            recordFailure(submission, principal, ip, exception.getMessage());
            throw new BusinessException(500, "规则执行失败：" + exception.getMessage());
        }
    }

    private void recordFailure(Submission submission, UserPrincipal principal, String ip, String message) {
        operationLogService.record(principal.getId(), principal.getUsername(), "RULE_ENGINE_EXECUTE",
                "SUBMISSION", submission == null ? null : submission.getId(), ip, "FAILED", message);
    }
}
