package com.superyoudi.businessservice.controller.student;


import com.superyoudi.businessservice.pojo.dto.CourseDTO;
import com.superyoudi.businessservice.pojo.entity.CourseData;
import com.superyoudi.businessservice.pojo.vo.*;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * GET /course/options
     * 获取课程数据列表
     */
    @GetMapping("/course/options")
    public Result<List<CourseData>> getCourseDataList() {
        List<CourseData> courseDataList = courseService.getCourseDataList();
        return Result.success(courseDataList);
    }

    /**
     * POST /course
     * 添加新课程
     */
    @PostMapping("/course")
    public Result<Long> addNewCourse(@RequestBody CourseDTO courseDTO) {
        Long courseId = courseService.addNewCourse(courseDTO);
        return Result.success(courseId);
    }

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @GetMapping("/course/{courseId}")
    public Result<CourseVO> getCourse(@PathVariable Long courseId) {
        CourseVO courseInfo = courseService.getCourseInfo(courseId);
        return Result.success(courseInfo);
    }

    /**
     * GET /course/info
     * 获取课程列表的课程统计信息
     */
    @GetMapping("/course/info")
    public Result<CourseStatisticsVO> getCourseStatistics(Long teacherId) {
        CourseStatisticsVO courseStatistics = courseService.getCourseStatistics(teacherId);
        return Result.success(courseStatistics);
    }


    /**
     * 获取课程学习路径
     * <p>
     * 如果没有，就生成并返回
     */
    @GetMapping("/course/path")
    public Result<LearningPathVO> getCourseLearningPath(@RequestParam Long studentId, @RequestParam Long courseId) {
        LearningPathVO learningPathInfo = courseService.getCourseLearningPath(studentId, courseId);
        return Result.success(learningPathInfo);
    }

    /**
     * 获取课程学习进度
     *
     * @param courseId  课程id
     * @param studentId 学生id
     * @return 课程学习进度
     */
    @GetMapping("/course/statistics/student")
    public Result<StudentCourseProgressVO> getCourseProgress(Long courseId, Long studentId) {
        StudentCourseProgressVO courseProgress = courseService.getCourseProgress(courseId, studentId);
        return Result.success(courseProgress);
    }

    /**
     * 加入课程
     *
     * @param courseId  课程id
     * @param studentId 学生id
     * @return 课程学习进度
     */
    @PostMapping("/course/join")
    public Result<Void> joinCourse(Long courseId, Long studentId) {
        courseService.joinCourse(studentId, courseId);
        return Result.success();
    }

    /**
     * 获取今日任务
     *
     * @param userId 用户id
     * @return 今日任务列表
     */
    @GetMapping("/study/today")
    public Result<List<TodayTaskVO>> getTodayTask(Long userId) {
        List<TodayTaskVO> voList = courseService.getTodayTaskList(userId);
        return Result.success(voList);
    }

    @GetMapping("/course/overview")
    public Result<List<CourseStudyOverviewVO>> getCourseStudyOverview(Long teacherId) {
        List<CourseStudyOverviewVO> courseStudyOverviews = courseService.getCourseStudyOverviews(teacherId);
        return Result.success(courseStudyOverviews);
    }
}