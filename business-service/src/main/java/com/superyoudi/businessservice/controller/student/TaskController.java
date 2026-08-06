package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.pojo.vo.ErrorQuestionVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;



    /**
     * PUT /task/submit 学生提交作业
     * @param userId 学生id
     * @param taskId 任务id
     * @return Result
     */
    @PutMapping("task/submit")
    public Result<String> submitTask(Long userId, Long taskId){
        taskService.submitTask(userId, taskId);
        return Result.success("提交成功");
    }

    /**
     * GET /task/overview 学生获取作业概览
     * @param userId 学生id
     * @return Result<StudentTaskOverviewVO>
     */
    @GetMapping("/task/overview")
    public Result<StudentTaskOverviewVO> getOverview( Long userId){
        StudentTaskOverviewVO vo = taskService.getOverview(userId);
        return Result.success(vo);
    }

    @GetMapping("/error/question")
    public Result<List<ErrorQuestionVO>> getErrorQuestionList(Long userId,Long courseId) {
        List<ErrorQuestionVO> voList = taskService.listUserErrorQuestion(userId, courseId);
        return Result.success(voList);
    }
}