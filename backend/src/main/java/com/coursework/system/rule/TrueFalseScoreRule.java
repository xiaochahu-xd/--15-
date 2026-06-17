package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Question;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrueFalseScoreRule implements Rule {

    @Override
    public String getName() {
        return "TrueFalseScoreRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null && "TRUE_FALSE".equals(context.getAssignment().getAssignmentType());
    }

    @Override
    public RuleResult execute(RuleContext context) {
        RuleResult result = new RuleResult();
        BigDecimal score = BigDecimal.ZERO;
        for (Question question : context.getQuestions()) {
            String studentAnswer = normalize(context.getStudentAnswers().get(String.valueOf(question.getId())));
            String standardAnswer = normalize(question.getStandardAnswer());
            if (!standardAnswer.isEmpty() && standardAnswer.equals(studentAnswer)) {
                score = score.add(question.getScore());
            }
        }
        result.setAutoScore(score);
        result.addMessage("判断题自动判分完成，得分：" + score);
        return result;
    }

    private String normalize(String answer) {
        if (answer == null) {
            return "";
        }
        String value = answer.trim().toUpperCase();
        if ("对".equals(answer) || "正确".equals(answer)) {
            return "TRUE";
        }
        if ("错".equals(answer) || "错误".equals(answer)) {
            return "FALSE";
        }
        return value;
    }
}
