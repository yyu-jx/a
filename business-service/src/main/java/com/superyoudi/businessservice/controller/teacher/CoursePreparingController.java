package com.superyoudi.businessservice.controller.teacher;

import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.UserFile;
import com.superyoudi.businessservice.service.FileService;
import com.superyoudi.businessservice.service.QuestionBaseService;
import com.superyoudi.common.dto.QuestionDTO;
import com.superyoudi.common.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 备课模块
 *
 * @author Sijayyx
 */
@RestController
@RequestMapping("/course/preparing")
class CoursePreparingController {

    private final QuestionBaseService questionBaseService;
    private final FileService fileService;

    CoursePreparingController(QuestionBaseService questionBaseService, FileService fileService) {
        this.questionBaseService = questionBaseService;
        this.fileService = fileService;
    }

    /// 获取用户文件列表
    @GetMapping("/files/{userId}")
    public Result<List<UserFile>> getUserFileList(@PathVariable Long userId) {
        return Result.success(fileService.getUserFileList(userId));
    }

    /// 删除用户文件
    @DeleteMapping("/files/{fileId}")
    public Result<Void> deleteUserFile(@PathVariable Long fileId) {
        fileService.deleteUserFile(fileId);
        return Result.success();
    }

    /// 获取题目列表
    @GetMapping("/questions")
    public Result<List<Question>> getQuestions(@RequestParam Long userId,
                                               @RequestParam Long courseId,
                                               @RequestParam String type) {

        return Result.success(questionBaseService.getQuestions(userId, courseId, type));

    }

    /// TODO 添加题目，包含属性与知识点关联
    @PostMapping("/questions")
    public Result<Void> addQuestion(@RequestBody QuestionDTO question) {
        questionBaseService.addQuestion(question);
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
