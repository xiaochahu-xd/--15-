package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Submission;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleModuleTest {

    @Test
    void lateSubmitRuleReturnsFalseBeforeDeadline() {
        Assignment assignment = assignment("TEXT", LocalDateTime.now().plusHours(1), "100");
        Submission submission = submission(1L, 10L, LocalDateTime.now(), "");
        RuleResult result = new LateSubmitRule().execute(context(assignment, submission));

        assertFalse(result.getLate());
    }

    @Test
    void lateSubmitRuleReturnsTrueAfterDeadline() {
        Assignment assignment = assignment("TEXT", LocalDateTime.now().minusMinutes(1), "100");
        Submission submission = submission(1L, 10L, LocalDateTime.now(), "");
        RuleResult result = new LateSubmitRule().execute(context(assignment, submission));

        assertTrue(result.getLate());
    }

    @Test
    void singleChoiceScoreRuleGivesFullScoreForCorrectAnswer() {
        Assignment assignment = assignment("SINGLE_CHOICE", LocalDateTime.now().plusHours(1), "10");
        RuleContext context = context(assignment, submission(1L, 10L, LocalDateTime.now(), ""));
        context.setQuestions(Collections.singletonList(question(100L, "SINGLE_CHOICE", "A", "10")));
        context.setStudentAnswers(answerMap("100", "A"));

        RuleResult result = new SingleChoiceScoreRule().execute(context);

        assertBigDecimalEquals("10", result.getAutoScore());
    }

    @Test
    void singleChoiceScoreRuleGivesZeroForWrongAnswer() {
        Assignment assignment = assignment("SINGLE_CHOICE", LocalDateTime.now().plusHours(1), "10");
        RuleContext context = context(assignment, submission(1L, 10L, LocalDateTime.now(), ""));
        context.setQuestions(Collections.singletonList(question(100L, "SINGLE_CHOICE", "A", "10")));
        context.setStudentAnswers(answerMap("100", "B"));

        RuleResult result = new SingleChoiceScoreRule().execute(context);

        assertBigDecimalEquals("0", result.getAutoScore());
    }

    @Test
    void trueFalseScoreRuleGivesFullScoreForCorrectAnswer() {
        Assignment assignment = assignment("TRUE_FALSE", LocalDateTime.now().plusHours(1), "10");
        RuleContext context = context(assignment, submission(1L, 10L, LocalDateTime.now(), ""));
        context.setQuestions(Collections.singletonList(question(100L, "TRUE_FALSE", "TRUE", "10")));
        context.setStudentAnswers(answerMap("100", "true"));

        RuleResult result = new TrueFalseScoreRule().execute(context);

        assertBigDecimalEquals("10", result.getAutoScore());
    }

    @Test
    void trueFalseScoreRuleGivesZeroForWrongAnswer() {
        Assignment assignment = assignment("TRUE_FALSE", LocalDateTime.now().plusHours(1), "10");
        RuleContext context = context(assignment, submission(1L, 10L, LocalDateTime.now(), ""));
        context.setQuestions(Collections.singletonList(question(100L, "TRUE_FALSE", "TRUE", "10")));
        context.setStudentAnswers(answerMap("100", "false"));

        RuleResult result = new TrueFalseScoreRule().execute(context);

        assertBigDecimalEquals("0", result.getAutoScore());
    }

    @Test
    void duplicateFileRuleMarksSameHashAcrossStudents() {
        Assignment assignment = assignment("FILE", LocalDateTime.now().plusHours(1), "100");
        Submission current = submission(1L, 10L, LocalDateTime.now(), "");
        Submission other = submission(2L, 11L, LocalDateTime.now(), "");
        RuleContext context = context(assignment, current);
        context.setCurrentFileRecords(Collections.singletonList(fileRecord(1L, "hash-a")));
        context.setAssignmentFileRecords(Arrays.asList(fileRecord(1L, "hash-a"), fileRecord(2L, "hash-a")));
        context.setAssignmentSubmissions(Arrays.asList(current, other));

        RuleResult result = new DuplicateFileRule().execute(context);

        assertTrue(result.isSuspectedDuplicate());
        assertEquals(Arrays.asList(1L, 2L), result.getDuplicateSubmissionIdsByHash().get("hash-a"));
    }

    @Test
    void duplicateFileRuleIgnoresDifferentHash() {
        Assignment assignment = assignment("FILE", LocalDateTime.now().plusHours(1), "100");
        Submission current = submission(1L, 10L, LocalDateTime.now(), "");
        Submission other = submission(2L, 11L, LocalDateTime.now(), "");
        RuleContext context = context(assignment, current);
        context.setCurrentFileRecords(Collections.singletonList(fileRecord(1L, "hash-a")));
        context.setAssignmentFileRecords(Arrays.asList(fileRecord(1L, "hash-a"), fileRecord(2L, "hash-b")));
        context.setAssignmentSubmissions(Arrays.asList(current, other));

        RuleResult result = new DuplicateFileRule().execute(context);

        assertFalse(result.isSuspectedDuplicate());
    }

    @Test
    void textSimilarityCalculatorReturnsOneForSameText() {
        TextSimilarityCalculator calculator = newCalculator();
        TextNormalizer normalizer = new TextNormalizer();

        BigDecimal similarity = calculator.calculate(
                normalizer.normalize("层次系统风格将系统划分为多个层次。"),
                normalizer.normalize("层次系统风格将系统划分为多个层次"));

        assertBigDecimalEquals("1.0000", similarity);
    }

    @Test
    void textSimilarityCalculatorMarksHighlySimilarText() {
        TextSimilarityCalculator calculator = newCalculator();
        TextNormalizer normalizer = new TextNormalizer();

        BigDecimal similarity = calculator.calculate(
                normalizer.normalize("层次系统风格将系统划分为多个层次每层向上提供服务"),
                normalizer.normalize("层次系统风格将系统划分为多个层次每层对上提供服务"));

        assertTrue(similarity.compareTo(new BigDecimal("0.9000")) >= 0);
    }

    @Test
    void textSimilarityCalculatorDoesNotMarkLowSimilarityText() {
        TextSimilarityCalculator calculator = newCalculator();
        TextNormalizer normalizer = new TextNormalizer();

        BigDecimal similarity = calculator.calculate(
                normalizer.normalize("层次系统风格强调分层组织和职责隔离"),
                normalizer.normalize("事件系统通过发布订阅机制完成通知分发"));

        assertTrue(similarity.compareTo(new BigDecimal("0.9000")) < 0);
    }

    private RuleContext context(Assignment assignment, Submission submission) {
        RuleContext context = new RuleContext();
        context.setAssignment(assignment);
        context.setSubmission(submission);
        return context;
    }

    private Assignment assignment(String type, LocalDateTime deadline, String totalScore) {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setAssignmentType(type);
        assignment.setDeadline(deadline);
        assignment.setTotalScore(new BigDecimal(totalScore));
        return assignment;
    }

    private Submission submission(Long id, Long studentId, LocalDateTime submitTime, String content) {
        Submission submission = new Submission();
        submission.setId(id);
        submission.setStudentId(studentId);
        submission.setSubmitTime(submitTime);
        submission.setContent(content);
        return submission;
    }

    private Question question(Long id, String type, String standardAnswer, String score) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionType(type);
        question.setStandardAnswer(standardAnswer);
        question.setScore(new BigDecimal(score));
        return question;
    }

    private FileRecord fileRecord(Long submissionId, String hash) {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setSubmissionId(submissionId);
        fileRecord.setFileHash(hash);
        return fileRecord;
    }

    private Map<String, String> answerMap(String questionId, String answer) {
        Map<String, String> answers = new HashMap<String, String>();
        answers.put(questionId, answer);
        return answers;
    }

    private TextSimilarityCalculator newCalculator() {
        SimilarityDetectionProperties properties = new SimilarityDetectionProperties();
        properties.setNgramSize(2);
        return new TextSimilarityCalculator(properties);
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
