package com.coursework.system.rule;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.similarity")
public class SimilarityDetectionProperties {
    private BigDecimal threshold = new BigDecimal("0.90");
    private int ngramSize = 2;

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold == null ? new BigDecimal("0.90") : threshold;
    }

    public int getNgramSize() {
        return ngramSize;
    }

    public void setNgramSize(int ngramSize) {
        this.ngramSize = Math.max(1, ngramSize);
    }
}
