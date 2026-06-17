package com.coursework.system.course.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CourseApplicationCreateRequest {
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称不能超过100个字符")
    private String courseName;
    @NotBlank(message = "课程代码不能为空")
    @Size(max = 50, message = "课程代码不能超过50个字符")
    private String courseCode;
    @Size(max = 1000, message = "课程描述不能超过1000个字符")
    private String description;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
