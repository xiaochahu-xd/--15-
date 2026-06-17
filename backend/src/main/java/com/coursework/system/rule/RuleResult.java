package com.coursework.system.rule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuleResult {
    private Boolean late;
    private BigDecimal autoScore;
    private boolean suspectedDuplicate;
    private Map<String, List<Long>> duplicateSubmissionIdsByHash = new LinkedHashMap<String, List<Long>>();
    private List<SimilarityMatchResult> similarityMatches = new ArrayList<SimilarityMatchResult>();
    private String gradeLevel;
    private List<String> messages = new ArrayList<String>();

    public Boolean getLate() {
        return late;
    }

    public void setLate(Boolean late) {
        this.late = late;
    }

    public BigDecimal getAutoScore() {
        return autoScore;
    }

    public void setAutoScore(BigDecimal autoScore) {
        this.autoScore = autoScore;
    }

    public boolean isSuspectedDuplicate() {
        return suspectedDuplicate;
    }

    public void setSuspectedDuplicate(boolean suspectedDuplicate) {
        this.suspectedDuplicate = suspectedDuplicate;
    }

    public Map<String, List<Long>> getDuplicateSubmissionIdsByHash() {
        return duplicateSubmissionIdsByHash;
    }

    public void setDuplicateSubmissionIdsByHash(Map<String, List<Long>> duplicateSubmissionIdsByHash) {
        this.duplicateSubmissionIdsByHash = duplicateSubmissionIdsByHash == null
                ? new LinkedHashMap<String, List<Long>>() : duplicateSubmissionIdsByHash;
    }

    public List<SimilarityMatchResult> getSimilarityMatches() {
        return similarityMatches;
    }

    public void setSimilarityMatches(List<SimilarityMatchResult> similarityMatches) {
        this.similarityMatches = similarityMatches == null ? new ArrayList<SimilarityMatchResult>() : similarityMatches;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages == null ? new ArrayList<String>() : messages;
    }

    public void addMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            messages.add(message);
        }
    }

    public void addDuplicate(String fileHash, List<Long> submissionIds) {
        suspectedDuplicate = true;
        duplicateSubmissionIdsByHash.put(fileHash, submissionIds);
    }

    public void addSimilarityMatch(SimilarityMatchResult match) {
        if (match != null) {
            suspectedDuplicate = true;
            similarityMatches.add(match);
        }
    }

    public void merge(RuleResult other) {
        if (other == null) {
            return;
        }
        if (other.getLate() != null) {
            late = other.getLate();
        }
        if (other.getAutoScore() != null) {
            autoScore = autoScore == null ? other.getAutoScore() : autoScore.add(other.getAutoScore());
        }
        if (other.isSuspectedDuplicate()) {
            suspectedDuplicate = true;
        }
        duplicateSubmissionIdsByHash.putAll(other.getDuplicateSubmissionIdsByHash());
        similarityMatches.addAll(other.getSimilarityMatches());
        if (other.getGradeLevel() != null) {
            gradeLevel = other.getGradeLevel();
        }
        messages.addAll(other.getMessages());
    }
}
