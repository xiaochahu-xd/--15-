package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleContext {
    private Assignment assignment;
    private Submission submission;
    private List<Question> questions = new ArrayList<Question>();
    private Map<String, String> studentAnswers = new HashMap<String, String>();
    private List<FileRecord> currentFileRecords = new ArrayList<FileRecord>();
    private List<FileRecord> assignmentFileRecords = new ArrayList<FileRecord>();
    private List<Submission> assignmentSubmissions = new ArrayList<Submission>();

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions == null ? new ArrayList<Question>() : questions;
    }

    public Map<String, String> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(Map<String, String> studentAnswers) {
        this.studentAnswers = studentAnswers == null ? new HashMap<String, String>() : studentAnswers;
    }

    public List<FileRecord> getCurrentFileRecords() {
        return currentFileRecords;
    }

    public void setCurrentFileRecords(List<FileRecord> currentFileRecords) {
        this.currentFileRecords = currentFileRecords == null ? new ArrayList<FileRecord>() : currentFileRecords;
    }

    public List<FileRecord> getAssignmentFileRecords() {
        return assignmentFileRecords;
    }

    public void setAssignmentFileRecords(List<FileRecord> assignmentFileRecords) {
        this.assignmentFileRecords = assignmentFileRecords == null ? new ArrayList<FileRecord>() : assignmentFileRecords;
    }

    public List<Submission> getAssignmentSubmissions() {
        return assignmentSubmissions;
    }

    public void setAssignmentSubmissions(List<Submission> assignmentSubmissions) {
        this.assignmentSubmissions = assignmentSubmissions == null ? new ArrayList<Submission>() : assignmentSubmissions;
    }
}
