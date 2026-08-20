package com.superyoudi.businessservice.controller.common;

import com.superyoudi.businessservice.pojo.dto.TaskQuestion;
import com.superyoudi.businessservice.pojo.entity.TaskList;
import com.superyoudi.businessservice.pojo.vo.CourseVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CourseService courseService;
    private final TaskService taskService;


    /**
     * 获取用户课程列表
     *
     * @param userId 用户id
     * @return 用户课程列表
     */
    @GetMapping("/course/list")
    public Result<List<CourseVO>> getCourseList(Long userId) {
        List<CourseVO> voList = courseService.getUserCourseList(userId);
        return Result.success(voList);
    }


    /**
     * 学生/教师 任务分页列表
     *
     * @param userId   用户id（学生/教师）
     * @param courseId 可选，课程筛选
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param status   可选，前端筛选提交状态
     */
    @GetMapping("/task")
    public Result<Page<TaskList>> getTaskList(
            @RequestParam Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status
    ) {
        Page<TaskList> page = taskService.getTaskPage(userId, courseId, pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 获取任务题目列表
     *
     * @param taskId 任务id
     * @return 任务问题列表
     */
    @GetMapping("/task/question")
    public Result<List<TaskQuestion>> getTaskQuestion(@RequestParam Long taskId) {
        List<TaskQuestion> list = taskService.getTaskQuestionList(taskId);
        return Result.success(list);
    }
}
