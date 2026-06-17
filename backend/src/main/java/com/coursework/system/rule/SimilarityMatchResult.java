package com.coursework.system.rule;

import java.math.BigDecimal;

public class SimilarityMatchResult {
    private Long matchedSubmissionId;
    private BigDecimal similarityScore;
    private String matchedStudentName;

    public SimilarityMatchResult() {
    }

    public SimilarityMatchResult(Long matchedSubmissionId, BigDecimal similarityScore, String matchedStudentName) {
        this.matchedSubmissionId = matchedSubmissionId;
        this.similarityScore = similarityScore;
        this.matchedStudentName = matchedStudentName;
    }

    public Long getMatchedSubmissionId() {
        return matchedSubmissionId;
    }

    public void setMatchedSubmissionId(Long matchedSubmissionId) {
        this.matchedSubmissionId = matchedSubmissionId;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getMatchedStudentName() {
        return matchedStudentName;
    }

    public void setMatchedStudentName(String matchedStudentName) {
        this.matchedStudentName = matchedStudentName;
    }
}
