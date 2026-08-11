package com.superyoudi.businessservice.controller.teacher;

import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.service.QuestionBaseService;
import com.superyoudi.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 备课模块
 *
 * @author Sijayyx
 */
@Controller
@RequestMapping("/course/preparing")
class CoursePreparingController {

    private final QuestionBaseService questionBaseService;

    CoursePreparingController(QuestionBaseService questionBaseService) {
        this.questionBaseService = questionBaseService;
    }

    /// 获取题目列表
    @GetMapping("/questions")
    public Result<List<Question>> getQuestions(@RequestParam Long userId,
                                               @RequestParam Long courseId,
                                               @RequestParam String type) {

        return Result.success(questionBaseService.getQuestions(userId, courseId, type));

    }

    /// 添加题目
    @PostMapping("/questions")
    public Result<Void> addQuestion(@RequestBody Question question) {
        questionBaseService.addQuestion(question, null);
        return Result.success();
    }

    /// 修改题目
    @PutMapping("/questions")
    public Result<Void> updateQuestion(@RequestBody Question question) {
        questionBaseService.updateQuestion(question);
        return Result.success();
    }

    /// 根据ID删除题目
    @DeleteMapping("/questions")
    public Result<Void> deleteQuestion(@RequestBody Question question) {
        questionBaseService.deleteQuestion(question.getId());
        return Result.success();
    }
}
