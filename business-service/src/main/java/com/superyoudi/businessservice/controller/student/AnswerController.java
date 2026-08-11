package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * AI 助手生成题目解析思路
     * @param questionId
     * @param taskId
     * @param studentId
     * @return
     */
    @GetMapping("/ai/suggestions/help")
    public Flux<String> getAiHelp(Long questionId, Long taskId, Long studentId) {
        return answerService.getQuestionHelpStream(questionId, taskId, studentId);
    }
}
