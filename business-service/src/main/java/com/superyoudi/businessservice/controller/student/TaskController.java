package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.vo.StudentAnswerVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.dto.ErrorQuestionVO;
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
     * GET /task
     * 修改/布置任务
     */
    @PostMapping("/task")
    public Result<Void> saveTaskData(@RequestBody TaskDTO taskDTO) {
        taskService.saveTaskData(taskDTO);
        return Result.success();
    }

    /**
     * PUT /task/submit 学生提交作业
     *
     * @param userId 学生id
     * @param taskId 任务id
     * @return Result
     */
    @PutMapping("/task/submit")
    public Result<String> submitTask(Long userId, Long taskId) {
        taskService.submitTask(userId, taskId);
        return Result.success("提交成功");
    }

    /**
     * GET /task/overview 学生获取作业概览
     *
     * @param userId 学生id
     * @return Result<StudentTaskOverviewVO>
     */
    @GetMapping("/task/overview")
    public Result<StudentTaskOverviewVO> getOverview(Long userId) {
        StudentTaskOverviewVO vo = taskService.getOverview(userId);
        return Result.success(vo);
    }

    /**
     * GET /error/question 获取错题列表
     *
     * @param userId   学生id
     * @param courseId 课程id
     * @return Result<List<ErrorQuestionVO>>
     */
    @GetMapping("/error/question")
    public Result<List<ErrorQuestionVO>> getErrorQuestionList(Long userId, Long courseId) {
        List<ErrorQuestionVO> voList = taskService.listUserErrorQuestion(userId, courseId);
        return Result.success(voList);
    }


    /**
     * 生成ai诊断报告
     *
     * @param studentId 学生id
     * @return 诊断报告
     */
    @GetMapping("/ai/student/diagnose")
    public Result<String> diagnose(Long studentId) {
        return Result.success(taskService.diagnose(studentId));
    }

    /**
     * GET /task/answers 获取学生单题答题记录（批改）
     *
     * @param studentId    学生ID（可选）
     * @param submissionId 提交表ID（可选）
     * @return 答题记录列表
     */
    @GetMapping("/task/answers")
    public Result<List<StudentAnswerVO>> getStudentAnswers(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long submissionId) {
        return Result.success(taskService.getStudentAnswers(studentId, submissionId));
    }

    /**
     * PUT /task/answers 修改学生单题答题记录（批改打分）
     *
     * @param dto 答题记录
     * @return Result
     */
    @PutMapping("/task/answers")
    public Result<Void> updateStudentAnswer(@RequestBody StudentAnswerVO dto) {
        taskService.updateStudentAnswer(dto);
        return Result.success();
    }

    /**
     * GET /task/submissions 获取所有学生的提交表（批改）
     *
     * @param taskId 任务ID（可选）
     * @return 提交记录列表
     */
    @GetMapping("/task/submissions")
    public Result<List<StudentSubmission>> getSubmissions(@RequestParam(required = false) Long taskId) {
        return Result.success(taskService.getSubmissions(taskId));
    }


}
