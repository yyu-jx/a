package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

@Data
public class StudentCourseKey {

    private Long studentId;
    private Long courseId;

    public StudentCourseKey() {
    }

    public StudentCourseKey(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }


}