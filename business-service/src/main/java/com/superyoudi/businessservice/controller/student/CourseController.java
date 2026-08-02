package com.superyoudi.businessservice.controller.student;


import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 获取单门课程详情
     * @param courseId 课程id
     * @return 课程信息
     */
    @GetMapping("/course/{courseId}")
    public Result<CoursesVO> getCourse(@PathVariable Long courseId) {
        CoursesVO courseInfo = courseService.getCourseInfo(courseId);
        return Result.success(courseInfo);
    }
}