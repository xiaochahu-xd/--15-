package com.coursework.system.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.entity.Question;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.assignment.service.QuestionService;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.entity.CourseApplication;
import com.coursework.system.course.entity.CourseMember;
import com.coursework.system.course.service.CourseApplicationService;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.duplicate.service.DuplicateRecordService;
import com.coursework.system.notification.service.NotificationService;
import com.coursework.system.rule.RuleEngine;
import com.coursework.system.submission.entity.FileRecord;
import com.coursework.system.submission.entity.Grade;
import com.coursework.system.submission.entity.Submission;
import com.coursework.system.submission.service.FileRecordService;
import com.coursework.system.submission.service.GradeService;
import com.coursework.system.submission.service.SubmissionService;
import com.coursework.system.user.entity.User;
import com.coursework.system.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {
    private static final String COURSE_CODE = "SWA-DEMO-2026";

    private final UserService userService;
    private final CourseApplicationService courseApplicationService;
    private final CourseService courseService;
    private final CourseMemberService courseMemberService;
    private final AssignmentService assignmentService;
    private final QuestionService questionService;
    private final SubmissionService submissionService;
    private final FileRecordService fileRecordService;
    private final GradeService gradeService;
    private final DuplicateRecordService duplicateRecordService;
    private final RuleEngine ruleEngine;
    private final NotificationService notificationService;

    public DemoDataInitializer(UserService userService,
                               CourseApplicationService courseApplicationService,
                               CourseService courseService,
                               CourseMemberService courseMemberService,
                               AssignmentService assignmentService,
                               QuestionService questionService,
                               SubmissionService submissionService,
                               FileRecordService fileRecordService,
                               GradeService gradeService,
                               DuplicateRecordService duplicateRecordService,
                               RuleEngine ruleEngine,
                               NotificationService notificationService) {
        this.userService = userService;
        this.courseApplicationService = courseApplicationService;
        this.courseService = courseService;
        this.courseMemberService = courseMemberService;
        this.assignmentService = assignmentService;
        this.questionService = questionService;
        this.submissionService = submissionService;
        this.fileRecordService = fileRecordService;
        this.gradeService = gradeService;
        this.duplicateRecordService = duplicateRecordService;
        this.ruleEngine = ruleEngine;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        User admin = requireUser("admin");
        User teacher = requireUser("teacher01");
        User assistant = requireUser("assistant01");
        User student01 = requireUser("student01");
        User student02 = requireUser("student02");
        User student03 = requireUser("student03");

        CourseApplication application = ensureCourseApplication(teacher, admin);
        Course course = ensureCourse(application, teacher);
        ensureMember(course, teacher, "OWNER");
        ensureMember(course, assistant, "ASSISTANT");
        ensureMember(course, student01, "STUDENT");
        ensureMember(course, student02, "STUDENT");
        ensureMember(course, student03, "STUDENT");

        Assignment textAssignment = ensureAssignment(course, teacher,
                "第一次作业：架构风格分析", "TEXT",
                "分析层次系统、仓库、事件系统等体系结构风格的适用场景。", new BigDecimal("100"),
                LocalDateTime.now().minusDays(1));
        Assignment fileAssignment = ensureAssignment(course, teacher,
                "第二次作业：体系结构图提交", "FILE",
                "提交系统体系结构图文件，用于演示文件正文相似度核查，文件 Hash 作为辅助线索。", new BigDecimal("100"),
                LocalDateTime.now().plusDays(7));
        Assignment singleChoiceAssignment = ensureAssignment(course, teacher,
                "第三次作业：架构风格选择题", "SINGLE_CHOICE",
                "选择最符合题意的体系结构风格。", new BigDecimal("10"),
                LocalDateTime.now().plusDays(7));
        Assignment trueFalseAssignment = ensureAssignment(course, teacher,
                "第四次作业：质量属性判断题", "TRUE_FALSE",
                "判断质量属性描述是否正确。", new BigDecimal("10"),
                LocalDateTime.now().plusDays(7));

        Question singleChoiceQuestion = ensureQuestion(singleChoiceAssignment, "SINGLE_CHOICE",
                "适合全局共享数据管理的典型体系结构风格是？",
                "[{\"label\":\"A\",\"text\":\"仓库风格\"},{\"label\":\"B\",\"text\":\"管道-过滤器\"},{\"label\":\"C\",\"text\":\"客户端-服务器\"}]",
                "A", new BigDecimal("10"));
        Question trueFalseQuestion = ensureQuestion(trueFalseAssignment, "TRUE_FALSE",
                "事件系统风格有利于降低事件发布者与订阅者之间的直接耦合。",
                null, "TRUE", new BigDecimal("10"));

        Submission textSubmission01 = ensureSubmission(textAssignment, student01,
                "我认为本系统可以用层次系统组织前后端，用仓库风格管理数据库，用事件系统完成通知。",
                textAssignment.getDeadline().minusHours(6));
        ensurePendingGrade(textSubmission01);
        ensureManualGrade(textSubmission01, teacher, new BigDecimal("92"), "分析完整，能联系项目中的规则系统和事件系统。");

        Submission textSubmission02 = ensureSubmission(textAssignment, student02,
                "本系统包含表示层、业务层和数据层，也包含面向对象模块划分。",
                textAssignment.getDeadline().plusHours(5));
        ensurePendingGrade(textSubmission02);
        ruleEngine.evaluateAndApply(textAssignment, textSubmission02);

        Submission fileSubmission01 = ensureSubmission(fileAssignment, student01, "体系结构图版本 A", LocalDateTime.now().minusHours(2));
        ensureFileRecord(fileAssignment, fileSubmission01, "architecture-demo.png", "same-architecture-diagram-content");
        ensurePendingGrade(fileSubmission01);
        ruleEngine.evaluateAndApply(fileAssignment, fileSubmission01);

        Submission fileSubmission03 = ensureSubmission(fileAssignment, student03, "体系结构图版本 A 复用", LocalDateTime.now().minusHours(1));
        ensureFileRecord(fileAssignment, fileSubmission03, "architecture-copy.png", "same-architecture-diagram-content");
        ensurePendingGrade(fileSubmission03);
        ruleEngine.evaluateAndApply(fileAssignment, fileSubmission03);

        Submission singleChoiceSubmission = ensureSubmission(singleChoiceAssignment, student01,
                "{\"" + singleChoiceQuestion.getId() + "\":\"A\"}", LocalDateTime.now().minusMinutes(50));
        ruleEngine.evaluateAndApply(singleChoiceAssignment, singleChoiceSubmission);

        Submission trueFalseSubmission = ensureSubmission(trueFalseAssignment, student02,
                "{\"" + trueFalseQuestion.getId() + "\":\"FALSE\"}", LocalDateTime.now().minusMinutes(40));
        ruleEngine.evaluateAndApply(trueFalseAssignment, trueFalseSubmission);

        ensureDemoNotifications(admin, teacher, assistant, student01, student02, student03);
    }

    private CourseApplication ensureCourseApplication(User teacher, User admin) {
        CourseApplication existing = courseApplicationService.getOne(new QueryWrapper<CourseApplication>()
                .eq("course_code", COURSE_CODE), false);
        if (existing != null) {
            return existing;
        }
        CourseApplication application = new CourseApplication();
        application.setTeacherId(teacher.getId());
        application.setCourseName("软件体系结构");
        application.setCourseCode(COURSE_CODE);
        application.setDescription("用于课程项目答辩演示的示例课程。");
        application.setStatus("APPROVED");
        application.setReviewedBy(admin.getId());
        application.setCreatedAt(LocalDateTime.now().minusDays(10));
        application.setReviewedAt(LocalDateTime.now().minusDays(9));
        courseApplicationService.save(application);
        return application;
    }

    private Course ensureCourse(CourseApplication application, User teacher) {
        Course course = courseService.getByCourseCode(COURSE_CODE);
        if (course != null) {
            return course;
        }
        course = new Course();
        course.setCourseName(application.getCourseName());
        course.setCourseCode(application.getCourseCode());
        course.setDescription(application.getDescription());
        course.setOwnerId(teacher.getId());
        course.setStatus("ACTIVE");
        course.setCreatedAt(LocalDateTime.now().minusDays(9));
        courseService.save(course);
        return course;
    }

    private Assignment ensureAssignment(Course course, User teacher, String title, String type,
                                        String description, BigDecimal score, LocalDateTime deadline) {
        Assignment assignment = assignmentService.getOne(new QueryWrapper<Assignment>()
                .eq("course_id", course.getId())
                .eq("title", title)
                .ne("status", "DELETED"), false);
        if (assignment != null) {
            return assignment;
        }
        assignment = new Assignment();
        assignment.setCourseId(course.getId());
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setAssignmentType(type);
        assignment.setDeadline(deadline);
        assignment.setTotalScore(score);
        assignment.setAllowLate(1);
        assignment.setStatus("PUBLISHED");
        assignment.setCreatedBy(teacher.getId());
        assignment.setCreatedAt(LocalDateTime.now().minusDays(5));
        assignmentService.save(assignment);
        return assignment;
    }

    private Question ensureQuestion(Assignment assignment, String type, String content,
                                    String options, String answer, BigDecimal score) {
        Question question = questionService.getOne(new QueryWrapper<Question>()
                .eq("assignment_id", assignment.getId())
                .eq("content", content), false);
        if (question != null) {
            return question;
        }
        question = new Question();
        question.setAssignmentId(assignment.getId());
        question.setQuestionType(type);
        question.setContent(content);
        question.setOptions(options);
        question.setStandardAnswer(answer);
        question.setScore(score);
        questionService.save(question);
        return question;
    }

    private Submission ensureSubmission(Assignment assignment, User student, String content, LocalDateTime submitTime) {
        Submission submission = submissionService.getOne(new QueryWrapper<Submission>()
                .eq("assignment_id", assignment.getId())
                .eq("student_id", student.getId())
                .eq("content", content), false);
        if (submission != null) {
            return submission;
        }
        submission = new Submission();
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(student.getId());
        submission.setContent(content);
        submission.setSubmitTime(submitTime);
        submission.setLate(0);
        submission.setStatus("SUBMITTED");
        submissionService.save(submission);
        return submission;
    }

    private void ensureFileRecord(Assignment assignment, Submission submission,
                                  String fileName, String content) throws Exception {
        if (fileRecordService.count(new QueryWrapper<FileRecord>().eq("submission_id", submission.getId())) > 0) {
            return;
        }
        Path root = Paths.get("uploads").toAbsolutePath().normalize();
        Path folder = root.resolve("demo").resolve("assignment-" + assignment.getId()).resolve(LocalDate.now().toString());
        Files.createDirectories(folder);
        Path target = folder.resolve(fileName).normalize();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Files.write(target, bytes);

        FileRecord record = new FileRecord();
        record.setSubmissionId(submission.getId());
        record.setFileName(fileName);
        record.setFilePath(target.toString());
        record.setFileSize((long) bytes.length);
        record.setFileHash(sha256(bytes));
        record.setUploadedAt(submission.getSubmitTime());
        fileRecordService.save(record);
    }

    private void ensurePendingGrade(Submission submission) {
        if (gradeService.getBySubmissionId(submission.getId()) == null) {
            gradeService.createPendingGrade(submission.getId());
        }
    }

    private void ensureManualGrade(Submission submission, User grader, BigDecimal score, String comment) {
        Grade grade = gradeService.getBySubmissionId(submission.getId());
        if (grade == null) {
            grade = new Grade();
            grade.setSubmissionId(submission.getId());
        }
        grade.setGraderId(grader.getId());
        grade.setScore(score);
        grade.setComment(comment);
        grade.setGradedAt(LocalDateTime.now().minusDays(1));
        grade.setGradeStatus("GRADED");
        gradeService.saveOrUpdate(grade);
        submission.setFinalScore(score);
        submission.setStatus("GRADED");
        submissionService.updateById(submission);
    }

    private void ensureMember(Course course, User user, String memberRole) {
        CourseMember member = courseMemberService.getByCourseIdAndUserId(course.getId(), user.getId());
        if (member == null) {
            member = new CourseMember();
            member.setCourseId(course.getId());
            member.setUserId(user.getId());
            member.setJoinedAt(LocalDateTime.now().minusDays(8));
        }
        member.setMemberRole(memberRole);
        member.setStatus(1);
        courseMemberService.saveOrUpdate(member);
    }

    private void ensureDemoNotifications(User admin, User teacher, User assistant, User student01, User student02, User student03) {
        List<User> users = Arrays.asList(admin, teacher, assistant, student01, student02, student03);
        for (User user : users) {
            if (notificationService.count(new QueryWrapper<com.coursework.system.notification.entity.Notification>()
                    .eq("receiver_id", user.getId())
                    .like("title", "演示数据")) == 0) {
                notificationService.create(user.getId(), "DEMO_DATA_READY", "演示数据已准备",
                        "系统已初始化课程、作业、提交、批改和统计演示数据。");
            }
        }
    }

    private User requireUser(String username) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new IllegalStateException("演示账号不存在：" + username);
        }
        return user;
    }

    private String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte value : hashed) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
