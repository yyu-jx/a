package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.Courses;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;

public interface CourseService {

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    CoursesVO getCourseInfo(Integer courseId);
}
