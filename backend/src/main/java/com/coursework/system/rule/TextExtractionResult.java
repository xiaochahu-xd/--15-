package com.coursework.system.rule;

public class TextExtractionResult {
    private final boolean success;
    private final String text;
    private final String message;

    private TextExtractionResult(boolean success, String text, String message) {
        this.success = success;
        this.text = text;
        this.message = message;
    }

    public static TextExtractionResult success(String text) {
        return new TextExtractionResult(true, text, null);
    }

    public static TextExtractionResult failure(String message) {
        return new TextExtractionResult(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getText() {
        return text;
    }

    public String getMessage() {
        return message;
    }
}
