package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.pojo.vo.StudentCourseProgressVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final StudentCourseProgressMapper studentCourseProgressesMapper;

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Override
    public CoursesVO getCourseInfo(Long courseId) {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            // 抛出业务异常：课程不存在
            throw new CourseNotExistException("课程不存在");
        }

        CoursesVO vo = new CoursesVO();
        BeanUtils.copyProperties(course, vo);

        return vo;
    }


    /**
     * 获取学生课程进度
     *
     * @param courseId  课程id
     * @param studentId 学生id
     * @return 学生课程进度
     */
    @Override
    public StudentCourseProgressVO getCourseProgress(Long courseId, Long studentId) {

        List<Long> courseIdList = List.of(courseId);
        List<StudentCourseProgress> progressList = studentCourseProgressesMapper.selectProgressByStudentAndCourseIds(studentId, courseIdList);
        if (CollectionUtils.isEmpty(progressList)) {
            throw new CourseNotExistException("课程不存在");
        }

        StudentCourseProgressVO vo = new StudentCourseProgressVO();
        BeanUtils.copyProperties(progressList.get(0), vo);

        return vo;
    }


    /**
     * 加入课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     */
    @Override
    public void joinCourse(Long studentId, Long courseId) {
        // 1. 判断学生是否已经加入该课程
        Integer count = courseMapper.countStudentJoinCourse(studentId, courseId);
        if (count > 0) {
            throw new StudentRepeatJoinCourseException("您已加入该课程，请勿重复加入");
        }

        // 2. 校验课程是否真实存在
        Course course = courseMapper.selectCourseById(courseId);
        if (Objects.isNull(course)) {
            throw new CourseNotExistException("课程不存在");
        }

        courseMapper.joinCourse(studentId, courseId);
    }
}
