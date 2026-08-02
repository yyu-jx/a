package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.CoursesMapper;
import com.superyoudi.businessservice.pojo.entity.Courses;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.exception.CourseNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CoursesMapper coursesMapper;

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Override
    public CoursesVO getCourseInfo(Long courseId) {
        Courses courses = coursesMapper.selectCourseDetailById(courseId);
        if (courses == null) {
            // 抛出业务异常：课程不存在
            throw new CourseNotExistException("课程不存在");
        }

        CoursesVO vo = new CoursesVO();
        BeanUtils.copyProperties(courses, vo);

        return vo;
    }
}
