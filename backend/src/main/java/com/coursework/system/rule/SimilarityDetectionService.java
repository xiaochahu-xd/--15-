package com.coursework.system.rule;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimilarityDetectionService {
    private final TextExtractor textExtractor;
    private final TextNormalizer textNormalizer;
    private final TextSimilarityCalculator similarityCalculator;
    private final SimilarityDetectionProperties properties;
    private final FileRecordService fileRecordService;
    private final DuplicateRecordService duplicateRecordService;
    private final UserService userService;
    private final OperationLogService operationLogService;

    public SimilarityDetectionService(TextExtractor textExtractor,
                                      TextNormalizer textNormalizer,
                                      TextSimilarityCalculator similarityCalculator,
                                      SimilarityDetectionProperties properties,
                                      FileRecordService fileRecordService,
                                      DuplicateRecordService duplicateRecordService,
                                      UserService userService,
                                      OperationLogService operationLogService) {
        this.textExtractor = textExtractor;
        this.textNormalizer = textNormalizer;
        this.similarityCalculator = similarityCalculator;
        this.properties = properties;
        this.fileRecordService = fileRecordService;
        this.duplicateRecordService = duplicateRecordService;
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

    public List<SimilarityMatchResult> detectAndRecord(RuleContext context) {
        List<SimilarityMatchResult> matches = new ArrayList<SimilarityMatchResult>();
        Assignment assignment = context.getAssignment();
        Submission current = context.getSubmission();
        if (assignment == null || current == null || !supports(assignment)) {
            return matches;
        }
        TextExtractionResult currentExtraction = textExtractor.extract(assignment, current, context.getCurrentFileRecords());
        if (!currentExtraction.isSuccess()) {
            recordExtractionNotice(current, assignment, currentExtraction.getMessage());
            return matches;
        }
        String currentText = textNormalizer.normalize(currentExtraction.getText());
        if (currentText.isEmpty()) {
            recordExtractionNotice(current, assignment, "提取后的文本为空，未参与相似度检测");
            return matches;
        }

        for (Submission candidate : context.getAssignmentSubmissions()) {
            if (candidate == null
                    || candidate.getId() == null
                    || candidate.getId().equals(current.getId())
                    || current.getStudentId().equals(candidate.getStudentId())) {
                continue;
            }
            TextExtractionResult candidateExtraction = textExtractor.extract(assignment, candidate, filesFor(context, candidate));
            if (!candidateExtraction.isSuccess()) {
                continue;
            }
            String candidateText = textNormalizer.normalize(candidateExtraction.getText());
            if (candidateText.isEmpty()) {
                continue;
            }
            BigDecimal similarity = similarityCalculator.calculate(currentText, candidateText);
            if (similarity.compareTo(properties.getThreshold()) >= 0) {
                duplicateRecordService.recordSimilarity(assignment.getId(), current, candidate,
                        similarity, properties.getThreshold(), "文本相似度达到核查阈值");
                matches.add(new SimilarityMatchResult(candidate.getId(), similarity, studentName(candidate.getStudentId())));
            }
        }
        return matches;
    }

    private boolean supports(Assignment assignment) {
        return "TEXT".equals(assignment.getAssignmentType()) || "FILE".equals(assignment.getAssignmentType());
    }

    private List<FileRecord> filesFor(RuleContext context, Submission submission) {
        if (submission.getId() != null && submission.getId().equals(context.getSubmission().getId())) {
            return context.getCurrentFileRecords();
        }
        return fileRecordService.listBySubmissionId(submission.getId());
    }

    private String studentName(Long studentId) {
        User user = userService.getById(studentId);
        return user == null ? "" : user.getRealName();
    }

    private void recordExtractionNotice(Submission submission, Assignment assignment, String message) {
        try {
            operationLogService.record(submission.getStudentId(), null, "SIMILARITY_TEXT_EXTRACT",
                    "ASSIGNMENT", assignment.getId(), "", "SKIPPED",
                    message == null ? "无法提取文本，未参与相似度检测" : message);
        } catch (RuntimeException ignored) {
            // Extraction notice should not affect submission.
        }
    }
}
