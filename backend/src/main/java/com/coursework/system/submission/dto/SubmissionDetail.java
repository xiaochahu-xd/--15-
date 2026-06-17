package com.coursework.system.submission.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmissionDetail extends SubmissionSummary {
    private List<FileRecordSummary> files = new ArrayList<FileRecordSummary>();

    public List<FileRecordSummary> getFiles() {
        return files;
    }

    public void setFiles(List<FileRecordSummary> files) {
        this.files = files;
    }
}
