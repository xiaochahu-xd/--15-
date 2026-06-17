package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Submission;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TextExtractor {

    public TextExtractionResult extract(Assignment assignment, Submission submission, List<FileRecord> files) {
        if (assignment == null || submission == null) {
            return TextExtractionResult.failure("缺少作业或提交上下文");
        }
        if ("TEXT".equals(assignment.getAssignmentType())) {
            String content = submission.getContent();
            if (content == null || content.trim().isEmpty()) {
                return TextExtractionResult.failure("文本作业提交内容为空，未参与相似度检测");
            }
            return TextExtractionResult.success(content);
        }
        if ("FILE".equals(assignment.getAssignmentType())) {
            return extractFromFiles(files);
        }
        return TextExtractionResult.failure("该作业类型不参与相似度检测");
    }

    private TextExtractionResult extractFromFiles(List<FileRecord> files) {
        if (files == null || files.isEmpty()) {
            return TextExtractionResult.failure("未找到上传文件，未参与相似度检测");
        }
        StringBuilder builder = new StringBuilder();
        StringBuilder skipped = new StringBuilder();
        for (FileRecord file : files) {
            TextExtractionResult result = extractFromFile(file);
            if (result.isSuccess()) {
                builder.append(result.getText()).append('\n');
            } else if (result.getMessage() != null) {
                if (skipped.length() > 0) {
                    skipped.append("；");
                }
                skipped.append(result.getMessage());
            }
        }
        if (builder.length() == 0) {
            return TextExtractionResult.failure(skipped.length() == 0
                    ? "无法提取文本，未参与相似度检测" : skipped.toString());
        }
        return TextExtractionResult.success(builder.toString());
    }

    private TextExtractionResult extractFromFile(FileRecord file) {
        if (file == null || file.getFilePath() == null) {
            return TextExtractionResult.failure("文件路径为空");
        }
        String fileName = file.getFileName() == null ? "" : file.getFileName().toLowerCase();
        Path path = Paths.get(file.getFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return TextExtractionResult.failure("文件不存在：" + file.getFileName());
        }
        try {
            if (isPlainText(fileName)) {
                byte[] bytes = Files.readAllBytes(path);
                return TextExtractionResult.success(new String(bytes, StandardCharsets.UTF_8));
            }
            if (fileName.endsWith(".docx")) {
                return TextExtractionResult.success(readDocx(path));
            }
            return TextExtractionResult.failure("无法提取文本，当前仅支持 txt、md、csv、代码文本和 docx：" + file.getFileName());
        } catch (Exception exception) {
            return TextExtractionResult.failure("无法提取文本：" + exception.getMessage());
        }
    }

    private boolean isPlainText(String fileName) {
        return fileName.endsWith(".txt")
                || fileName.endsWith(".md")
                || fileName.endsWith(".csv")
                || fileName.endsWith(".json")
                || fileName.endsWith(".xml")
                || fileName.endsWith(".java")
                || fileName.endsWith(".py")
                || fileName.endsWith(".js")
                || fileName.endsWith(".ts")
                || fileName.endsWith(".vue")
                || fileName.endsWith(".html")
                || fileName.endsWith(".css");
    }

    private String readDocx(Path path) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                builder.append(paragraph.getText()).append('\n');
            }
        }
        return builder.toString();
    }
}
