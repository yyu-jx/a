package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.vo.*;
import com.superyoudi.common.exception.CourseNotExistException;

import java.util.List;

public interface CourseService {

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    CourseVO getCourseInfo(Long courseId) throws CourseNotExistException;

    List<Course> getCoursesByTeacherId(Long teacherId);

    /**
     * 获取学生课程进度
     *
     * @param courseId  课程id
     * @param studentId 学生id
     * @return 学生课程进度
     */
    StudentCourseProgressVO getCourseProgress(Long courseId, Long studentId);

    List<CourseStudyOverviewVO> getCourseStudyOverviews(Long teacherId);

    /**
     * 加入课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     */
    void joinCourse(Long studentId, Long courseId);

    /**
     * 获取今日任务列表
     *
     * @param userId 用户id
     * @return 今日任务列表
     */
    List<TodayTaskVO> getTodayTaskList(Long userId);

    LearningPathVO getCourseLearningPath(Long studentId, Long courseId);

    void openCourse(Long courseId);

    /**
     * 获取用户课程列表
     *
     * @param userId 用户id
     * @return 用户课程列表
     */
    List<CourseVO> getUserCourseList(Long userId);
}
