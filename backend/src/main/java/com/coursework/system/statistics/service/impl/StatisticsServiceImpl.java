package com.coursework.system.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.common.exception.BusinessException;
import com.coursework.system.common.security.UserPrincipal;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.entity.DuplicateRecord;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.log.service.OperationLogService;
import com.coursework.system.statistics.dto.AssignmentStatistics;
import com.coursework.system.statistics.dto.CourseStatistics;
import com.coursework.system.statistics.dto.GradeExportRow;
import com.coursework.system.statistics.dto.ScoreBucket;
import com.coursework.system.statistics.entity.ExportRecord;
import com.coursework.system.statistics.mapper.ExportRecordMapper;
import com.coursework.system.statistics.service.StatisticsService;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.mapper.SubmissionMapper;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final AssignmentService assignmentService;
    private final SubmissionMapper submissionMapper;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final UserService userService;
    private final ExportRecordMapper exportRecordMapper;
    private final OperationLogService operationLogService;

    public StatisticsServiceImpl(CourseService courseService,
                                 CourseMemberService courseMemberService,
                                 AssignmentService assignmentService,
                                 SubmissionMapper submissionMapper,
                                 GradeService gradeService,
                                 DuplicateRecordService duplicateRecordService,
                                 UserService userService,
                                 ExportRecordMapper exportRecordMapper,
                                 OperationLogService operationLogService) {
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.assignmentService = assignmentService;
        this.submissionMapper = submissionMapper;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.userService = userService;
        this.exportRecordMapper = exportRecordMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public CourseStatistics getCourseStatistics(Long courseId, UserPrincipal principal) {
        Course course = requireCourseForStatistics(courseId, principal);
        List<Assignment> assignments = listAssignments(courseId);
        List<Submission> submissions = listSubmissionsByAssignments(assignments);
        List<BigDecimal> scores = collectScores(submissions);
        int studentCount = countStudents(courseId);
        int expectedSubmissions = studentCount * assignments.size();
        int submittedPairs = countDistinctAssignmentStudentPairs(submissions);
        int latePairs = countLateDistinctAssignmentStudentPairs(submissions);

        CourseStatistics statistics = new CourseStatistics();
        statistics.setCourseId(course.getId());
        statistics.setCourseName(course.getCourseName());
        statistics.setStudentCount(studentCount);
        statistics.setAssignmentCount(assignments.size());
        statistics.setSubmissionCount(submittedPairs);
        statistics.setSubmissionRate(rate(submittedPairs, expectedSubmissions));
        statistics.setLateCount(latePairs);
        statistics.setLateRate(rate(latePairs, submittedPairs));
        statistics.setAverageScore(average(scores));
        statistics.setHighestScore(max(scores));
        statistics.setLowestScore(min(scores));
        statistics.setDuplicateCount(countDuplicatesForAssignments(assignments));
        return statistics;
    }

    @Override
    public AssignmentStatistics getAssignmentStatistics(Long assignmentId, UserPrincipal principal) {
        Assignment assignment = assignmentService.getActiveAssignment(assignmentId);
        Course course = requireCourseForStatistics(assignment.getCourseId(), principal);
        List<Submission> submissions = submissionMapper.selectList(new QueryWrapper<Submission>()
                .eq("assignment_id", assignmentId));
        List<BigDecimal> scores = collectScores(submissions);
        int studentCount = countStudents(assignment.getCourseId());
        int submittedStudents = countDistinctStudents(submissions);
        int lateStudents = countLateDistinctStudents(submissions);

        AssignmentStatistics statistics = new AssignmentStatistics();
        statistics.setAssignmentId(assignment.getId());
        statistics.setAssignmentTitle(assignment.getTitle());
        statistics.setCourseId(course.getId());
        statistics.setCourseName(course.getCourseName());
        statistics.setStudentCount(studentCount);
        statistics.setSubmissionCount(submittedStudents);
        statistics.setSubmissionRate(rate(submittedStudents, studentCount));
        statistics.setLateCount(lateStudents);
        statistics.setLateRate(rate(lateStudents, submittedStudents));
        statistics.setAverageScore(average(scores));
        statistics.setHighestScore(max(scores));
        statistics.setLowestScore(min(scores));
        statistics.setDuplicateCount((int) duplicateRecordService.count(new QueryWrapper<DuplicateRecord>()
                .eq("assignment_id", assignmentId)
                .eq("status", "SUSPECTED")));
        statistics.setScoreDistribution(distribution(scores));
        return statistics;
    }

    @Override
    public List<GradeExportRow> listCourseGrades(Long courseId, UserPrincipal principal) {
        requireCourseForStatistics(courseId, principal);
        return buildGradeRows(courseId);
    }

    @Override
    public byte[] exportCourseGrades(Long courseId, UserPrincipal principal, String ip) {
        Course course = requireCourseForExport(courseId, principal);
        List<GradeExportRow> rows = buildGradeRows(courseId);
        byte[] bytes = writeWorkbook(rows);
        try {
            Path folder = Paths.get("exports").toAbsolutePath().normalize();
            Files.createDirectories(folder);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "course-" + courseId + "-grades-" + timestamp + ".xlsx";
            Path target = folder.resolve(fileName).normalize();
            Files.write(target, bytes);

            ExportRecord record = new ExportRecord();
            record.setCourseId(courseId);
            record.setFileName(fileName);
            record.setFilePath(target.toString());
            record.setStatus("SUCCESS");
            record.setCreatedBy(principal.getId());
            record.setCreatedAt(LocalDateTime.now());
            exportRecordMapper.insert(record);

            operationLogService.record(principal.getId(), principal.getUsername(), "GRADES_EXPORT",
                    "COURSE", courseId, ip, "SUCCESS", "导出课程成绩：" + course.getCourseName());
            return bytes;
        } catch (Exception exception) {
            operationLogService.record(principal.getId(), principal.getUsername(), "GRADES_EXPORT",
                    "COURSE", courseId, ip, "FAILED", exception.getMessage());
            throw new BusinessException(500, "成绩导出失败：" + exception.getMessage());
        }
    }

    private List<GradeExportRow> buildGradeRows(Long courseId) {
        Course course = courseService.getById(courseId);
        List<Assignment> assignments = listAssignments(courseId);
        List<Submission> submissions = listSubmissionsByAssignments(assignments);
        Map<Long, Assignment> assignmentsById = new HashMap<Long, Assignment>();
        for (Assignment assignment : assignments) {
            assignmentsById.put(assignment.getId(), assignment);
        }
        Map<Long, Grade> gradesBySubmissionId = loadGrades(submissions);

        List<GradeExportRow> rows = new ArrayList<GradeExportRow>();
        for (Submission submission : submissions) {
            Assignment assignment = assignmentsById.get(submission.getAssignmentId());
            User student = userService.getById(submission.getStudentId());
            Grade grade = gradesBySubmissionId.get(submission.getId());
            User grader = grade == null || grade.getGraderId() == null ? null : userService.getById(grade.getGraderId());

            GradeExportRow row = new GradeExportRow();
            row.setStudentNo(student == null ? "" : student.getUsername());
            row.setStudentName(student == null ? "" : student.getRealName());
            row.setCourseName(course == null ? "" : course.getCourseName());
            row.setAssignmentName(assignment == null ? "" : assignment.getTitle());
            row.setSubmitTime(submission.getSubmitTime());
            row.setLate(submission.getLate() != null && submission.getLate() == 1);
            row.setSuspectedDuplicate(assignment != null
                    && duplicateRecordService.isSubmissionSuspectedDuplicate(assignment.getId(), submission.getId()));
            row.setAutoScore(submission.getAutoScore());
            row.setManualScore(grade != null && "GRADED".equals(grade.getGradeStatus()) ? grade.getScore() : null);
            row.setFinalScore(submission.getFinalScore());
            row.setComment(grade == null ? "" : grade.getComment());
            row.setGraderName(grader == null ? "" : grader.getRealName());
            rows.add(row);
        }
        return rows;
    }

    private byte[] writeWorkbook(List<GradeExportRow> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("成绩明细");
            CellStyle dateStyle = workbook.createCellStyle();
            short dateFormat = workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm");
            dateStyle.setDataFormat(dateFormat);

            List<String> headers = Arrays.asList("学号", "姓名", "课程名称", "作业名称", "提交时间", "是否迟交",
                    "是否高度相似", "自动判分", "人工评分", "最终成绩", "评语", "批改人");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }
            int rowIndex = 1;
            for (GradeExportRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(nullToEmpty(item.getStudentNo()));
                row.createCell(1).setCellValue(nullToEmpty(item.getStudentName()));
                row.createCell(2).setCellValue(nullToEmpty(item.getCourseName()));
                row.createCell(3).setCellValue(nullToEmpty(item.getAssignmentName()));
                row.createCell(4).setCellValue(item.getSubmitTime() == null ? "" : item.getSubmitTime().toString().replace("T", " "));
                row.createCell(5).setCellValue(Boolean.TRUE.equals(item.getLate()) ? "是" : "否");
                row.createCell(6).setCellValue(Boolean.TRUE.equals(item.getSuspectedDuplicate()) ? "是" : "否");
                row.createCell(7).setCellValue(toDouble(item.getAutoScore()));
                row.createCell(8).setCellValue(toDouble(item.getManualScore()));
                row.createCell(9).setCellValue(toDouble(item.getFinalScore()));
                row.createCell(10).setCellValue(nullToEmpty(item.getComment()));
                row.createCell(11).setCellValue(nullToEmpty(item.getGraderName()));
            }
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(500, "Excel 生成失败：" + exception.getMessage());
        }
    }

    private Course requireCourseForStatistics(Long courseId, UserPrincipal principal) {
        ensureLogin(principal);
        Course course = courseService.getById(courseId);
        if (course == null || !"ACTIVE".equals(course.getStatus())) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!(principal.getRoles().contains("ADMIN")
                || principal.getRoles().contains("TEACHER")
                || principal.getRoles().contains("ASSISTANT"))
                || !courseService.canViewCourse(courseId, principal)) {
            throw new BusinessException(403, "无权查看该课程统计");
        }
        return course;
    }

    private Course requireCourseForExport(Long courseId, UserPrincipal principal) {
        Course course = requireCourseForStatistics(courseId, principal);
        if (!(principal.getRoles().contains("ADMIN") || principal.getRoles().contains("TEACHER"))) {
            throw new BusinessException(403, "只有管理员或教师可以导出成绩");
        }
        return course;
    }

    private List<Assignment> listAssignments(Long courseId) {
        return assignmentService.list(new QueryWrapper<Assignment>()
                .eq("course_id", courseId)
                .ne("status", "DELETED")
                .orderByAsc("created_at"));
    }

    private List<Submission> listSubmissionsByAssignments(List<Assignment> assignments) {
        List<Long> assignmentIds = new ArrayList<Long>();
        for (Assignment assignment : assignments) {
            assignmentIds.add(assignment.getId());
        }
        if (assignmentIds.isEmpty()) {
            return new ArrayList<Submission>();
        }
        return submissionMapper.selectList(new QueryWrapper<Submission>()
                .in("assignment_id", assignmentIds)
                .orderByAsc("assignment_id")
                .orderByAsc("student_id"));
    }

    private int countStudents(Long courseId) {
        int count = 0;
        for (CourseMemberSummary member : courseMemberService.listActiveMembers(courseId)) {
            if ("STUDENT".equals(member.getMemberRole())) {
                count++;
            }
        }
        return count;
    }

    private int countDistinctStudents(List<Submission> submissions) {
        Set<Long> studentIds = new HashSet<Long>();
        for (Submission submission : submissions) {
            studentIds.add(submission.getStudentId());
        }
        return studentIds.size();
    }

    private int countLateDistinctStudents(List<Submission> submissions) {
        Set<Long> studentIds = new HashSet<Long>();
        for (Submission submission : submissions) {
            if (submission.getLate() != null && submission.getLate() == 1) {
                studentIds.add(submission.getStudentId());
            }
        }
        return studentIds.size();
    }

    private int countDistinctAssignmentStudentPairs(List<Submission> submissions) {
        Set<String> pairs = new HashSet<String>();
        for (Submission submission : submissions) {
            pairs.add(submission.getAssignmentId() + ":" + submission.getStudentId());
        }
        return pairs.size();
    }

    private int countLateDistinctAssignmentStudentPairs(List<Submission> submissions) {
        Set<String> pairs = new HashSet<String>();
        for (Submission submission : submissions) {
            if (submission.getLate() != null && submission.getLate() == 1) {
                pairs.add(submission.getAssignmentId() + ":" + submission.getStudentId());
            }
        }
        return pairs.size();
    }

    private int countDuplicatesForAssignments(List<Assignment> assignments) {
        int count = 0;
        for (Assignment assignment : assignments) {
            count += duplicateRecordService.count(new QueryWrapper<DuplicateRecord>()
                    .eq("assignment_id", assignment.getId())
                    .eq("status", "SUSPECTED"));
        }
        return count;
    }

    private Map<Long, Grade> loadGrades(List<Submission> submissions) {
        List<Long> submissionIds = new ArrayList<Long>();
        for (Submission submission : submissions) {
            submissionIds.add(submission.getId());
        }
        Map<Long, Grade> gradesBySubmissionId = new HashMap<Long, Grade>();
        if (submissionIds.isEmpty()) {
            return gradesBySubmissionId;
        }
        List<Grade> grades = gradeService.list(new QueryWrapper<Grade>().in("submission_id", submissionIds));
        for (Grade grade : grades) {
            gradesBySubmissionId.put(grade.getSubmissionId(), grade);
        }
        return gradesBySubmissionId;
    }

    private List<BigDecimal> collectScores(List<Submission> submissions) {
        List<BigDecimal> scores = new ArrayList<BigDecimal>();
        for (Submission submission : submissions) {
            if (submission.getFinalScore() != null) {
                scores.add(submission.getFinalScore());
            }
        }
        return scores;
    }

    private BigDecimal rate(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(numerator).multiply(new BigDecimal("100"))
                .divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> scores) {
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal score : scores) {
            total = total.add(score);
        }
        return total.divide(new BigDecimal(scores.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal max(List<BigDecimal> scores) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal score : scores) {
            if (score.compareTo(result) > 0) {
                result = score;
            }
        }
        return result;
    }

    private BigDecimal min(List<BigDecimal> scores) {
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = scores.get(0);
        for (BigDecimal score : scores) {
            if (score.compareTo(result) < 0) {
                result = score;
            }
        }
        return result;
    }

    private List<ScoreBucket> distribution(List<BigDecimal> scores) {
        int[] buckets = new int[]{0, 0, 0, 0, 0};
        for (BigDecimal score : scores) {
            int value = score.intValue();
            if (value < 60) buckets[0]++;
            else if (value < 70) buckets[1]++;
            else if (value < 80) buckets[2]++;
            else if (value < 90) buckets[3]++;
            else buckets[4]++;
        }
        return Arrays.asList(
                new ScoreBucket("0-59", buckets[0]),
                new ScoreBucket("60-69", buckets[1]),
                new ScoreBucket("70-79", buckets[2]),
                new ScoreBucket("80-89", buckets[3]),
                new ScoreBucket("90-100", buckets[4])
        );
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void ensureLogin(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
