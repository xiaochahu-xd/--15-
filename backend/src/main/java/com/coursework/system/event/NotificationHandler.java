package com.coursework.system.event;

import com.coursework.system.assignment.entity.Assignment;
import com.coursework.system.assignment.service.AssignmentService;
import com.coursework.system.course.dto.CourseMemberSummary;
import com.coursework.system.course.entity.Course;
import com.coursework.system.course.service.CourseMemberService;
import com.coursework.system.course.service.CourseService;
import com.coursework.system.notification.service.NotificationService;
import com.coursework.system.user.dto.UserSummary;
import com.coursework.system.user.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class NotificationHandler implements EventHandler {
    private final NotificationService notificationService;
    private final CourseMemberService courseMemberService;
    private final CourseService courseService;
    private final ObjectProvider<AssignmentService> assignmentServiceProvider;
    private final UserService userService;

    public NotificationHandler(NotificationService notificationService,
                               CourseMemberService courseMemberService,
                               CourseService courseService,
                               ObjectProvider<AssignmentService> assignmentServiceProvider,
                               UserService userService) {
        this.notificationService = notificationService;
        this.courseMemberService = courseMemberService;
        this.courseService = courseService;
        this.assignmentServiceProvider = assignmentServiceProvider;
        this.userService = userService;
    }

    @Override
    public boolean supports(SystemEvent event) {
        return event instanceof CourseApplicationCreatedEvent
                || event instanceof CourseApprovedEvent
                || event instanceof AssignmentPublishedEvent
                || event instanceof SubmissionCreatedEvent
                || event instanceof GradeCompletedEvent
                || event instanceof AssignmentReturnedEvent;
    }

    @Override
    public void handle(SystemEvent event) {
        if (event instanceof CourseApplicationCreatedEvent) {
            handleCourseApplicationCreated((CourseApplicationCreatedEvent) event);
        } else if (event instanceof CourseApprovedEvent) {
            handleCourseApproved((CourseApprovedEvent) event);
        } else if (event instanceof AssignmentPublishedEvent) {
            handleAssignmentPublished((AssignmentPublishedEvent) event);
        } else if (event instanceof SubmissionCreatedEvent) {
            handleSubmissionCreated((SubmissionCreatedEvent) event);
        } else if (event instanceof GradeCompletedEvent) {
            handleGradeCompleted((GradeCompletedEvent) event);
        } else if (event instanceof AssignmentReturnedEvent) {
            handleAssignmentReturned((AssignmentReturnedEvent) event);
        }
    }

    private void handleCourseApplicationCreated(CourseApplicationCreatedEvent event) {
        for (UserSummary user : userService.listSummaries()) {
            if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
                notificationService.create(user.getId(), "COURSE_APPLICATION",
                        "新的课程申请",
                        "教师 " + event.getTeacherUsername() + " 提交了课程“"
                                + event.getCourseName() + "（" + event.getCourseCode() + "）”创建申请，请及时审批。",
                        "COURSE_APPLICATION", event.getApplicationId(), null);
            }
        }
    }

    private void handleCourseApproved(CourseApprovedEvent event) {
        Course course = event.isApproved() ? courseService.getByCourseCode(event.getCourseCode()) : null;
        if (event.isApproved()) {
            Long courseId = course == null ? null : course.getId();
            String targetType = courseId == null ? "COURSE_APPLICATION" : "COURSE";
            Long targetId = courseId == null ? event.getApplicationId() : courseId;
            notificationService.create(event.getTeacherId(), "COURSE_APPROVAL",
                    "课程申请已通过",
                    "课程“" + event.getCourseName() + "”已审批通过。",
                    targetType, targetId, courseId);
        } else {
            notificationService.create(event.getTeacherId(), "COURSE_APPROVAL",
                    "课程申请被驳回",
                    "课程“" + event.getCourseName() + "”被驳回，原因：" + safeText(event.getRejectReason()),
                    "COURSE_APPLICATION", event.getApplicationId(), null);
        }
    }

    private void handleAssignmentPublished(AssignmentPublishedEvent event) {
        for (CourseMemberSummary member : courseMemberService.listActiveMembers(event.getCourseId())) {
            if ("STUDENT".equals(member.getMemberRole())) {
                notificationService.create(member.getUserId(), "ASSIGNMENT_PUBLISHED",
                        "新作业已发布",
                        "作业“" + event.getAssignmentTitle() + "”已发布，请按时完成。",
                        "ASSIGNMENT", event.getAssignmentId(), event.getCourseId());
            }
        }
    }

    private void handleSubmissionCreated(SubmissionCreatedEvent event) {
        Long courseId = resolveCourseId(event.getAssignmentId());
        notificationService.create(event.getStudentId(), "SUBMISSION_CREATED",
                "作业提交成功",
                "作业“" + event.getAssignmentTitle() + "”已提交成功。",
                "SUBMISSION", event.getSubmissionId(), courseId);
    }

    private void handleGradeCompleted(GradeCompletedEvent event) {
        Long courseId = resolveCourseId(event.getAssignmentId());
        notificationService.create(event.getStudentId(), "GRADE_COMPLETED",
                "作业已批改",
                "作业“" + event.getAssignmentTitle() + "”已完成批改，最终成绩为 "
                        + event.getScore() + "。",
                "GRADE", event.getSubmissionId(), courseId);
    }

    private void handleAssignmentReturned(AssignmentReturnedEvent event) {
        Long courseId = resolveCourseId(event.getAssignmentId());
        String reason = event.getReason() == null || event.getReason().trim().isEmpty()
                ? "请根据教师或助教要求修改后重新提交。"
                : event.getReason();
        notificationService.create(event.getStudentId(), "ASSIGNMENT_RETURNED",
                "作业已退回修改",
                "作业“" + event.getAssignmentTitle() + "”已被退回修改，原因：" + reason,
                "SUBMISSION", event.getSubmissionId(), courseId);
    }

    private Long resolveCourseId(Long assignmentId) {
        AssignmentService assignmentService = assignmentServiceProvider.getIfAvailable();
        if (assignmentService == null) {
            return null;
        }
        Assignment assignment = assignmentService.getById(assignmentId);
        return assignment == null ? null : assignment.getCourseId();
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "无" : value;
    }
}
