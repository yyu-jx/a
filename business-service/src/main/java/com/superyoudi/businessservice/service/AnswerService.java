package com.superyoudi.businessservice.service;

import reactor.core.publisher.Flux;

public interface AnswerService {

    /**
     * AI 助手生成题目解析思路
     * @param questionId
     * @param taskId
     * @param studentId
     * @return
     */
    Flux<String> getQuestionHelpStream(Long questionId, Long taskId, Long studentId);
}
