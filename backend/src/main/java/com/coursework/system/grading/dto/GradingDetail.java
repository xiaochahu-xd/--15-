package com.coursework.system.grading.dto;

import com.coursework.system.submission.dto.FileRecordSummary;

import java.util.ArrayList;
import java.util.List;

public class GradingDetail extends GradingItem {
    private String content;
    private List<FileRecordSummary> files = new ArrayList<FileRecordSummary>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FileRecordSummary> getFiles() {
        return files;
    }

    public void setFiles(List<FileRecordSummary> files) {
        this.files = files;
    }
}
