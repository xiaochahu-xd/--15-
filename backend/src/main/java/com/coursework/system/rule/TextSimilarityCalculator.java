package com.coursework.system.rule;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class TextSimilarityCalculator {
    private final SimilarityDetectionProperties properties;

    public TextSimilarityCalculator(SimilarityDetectionProperties properties) {
        this.properties = properties;
    }

    public BigDecimal calculate(String left, String right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (left.equals(right)) {
            return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        }
        Map<String, Integer> leftVector = vectorize(left);
        Map<String, Integer> rightVector = vectorize(right);
        if (leftVector.isEmpty() || rightVector.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        double dot = 0D;
        for (Map.Entry<String, Integer> entry : leftVector.entrySet()) {
            Integer rightValue = rightVector.get(entry.getKey());
            if (rightValue != null) {
                dot += entry.getValue() * rightValue;
            }
        }
        double leftNorm = norm(leftVector);
        double rightNorm = norm(rightVector);
        if (leftNorm == 0D || rightNorm == 0D) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        double value = dot / (leftNorm * rightNorm);
        return BigDecimal.valueOf(Math.max(0D, Math.min(1D, value))).setScale(4, RoundingMode.HALF_UP);
    }

    private Map<String, Integer> vectorize(String text) {
        Map<String, Integer> vector = new HashMap<String, Integer>();
        int n = Math.max(1, properties.getNgramSize());
        if (text.length() <= n) {
            vector.put(text, 1);
            return vector;
        }
        for (int index = 0; index <= text.length() - n; index++) {
            String token = text.substring(index, index + n);
            Integer count = vector.get(token);
            vector.put(token, count == null ? 1 : count + 1);
        }
        return vector;
    }

    private double norm(Map<String, Integer> vector) {
        double sum = 0D;
        for (Integer value : vector.values()) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}
