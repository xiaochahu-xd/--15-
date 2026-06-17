package com.coursework.system.statistics.dto;

public class ScoreBucket {
    private String label;
    private Integer count;

    public ScoreBucket() {
    }

    public ScoreBucket(String label, Integer count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
