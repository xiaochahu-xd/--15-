package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Question;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SingleChoiceScoreRule implements Rule {

    @Override
    public String getName() {
        return "SingleChoiceScoreRule";
    }

    @Override
    public boolean supports(RuleContext context) {
        return context.getAssignment() != null && "SINGLE_CHOICE".equals(context.getAssignment().getAssignmentType());
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
        result.addMessage("单选题自动判分完成，得分：" + score);
        return result;
    }

    private String normalize(String answer) {
        return answer == null ? "" : answer.trim().toUpperCase();
    }
}
