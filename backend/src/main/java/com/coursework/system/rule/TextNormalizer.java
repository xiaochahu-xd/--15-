package com.coursework.system.rule;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TextNormalizer {
    public String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}，。！？；：、“”‘’（）【】《》〈〉—…·￥]", "")
                .replaceAll("\\s+", "");
    }
}
