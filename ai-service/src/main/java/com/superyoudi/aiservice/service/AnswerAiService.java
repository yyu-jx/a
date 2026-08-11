package com.superyoudi.aiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AnswerAiService {

    private final ChatClient chatClient;

    private static final String QUESTION_HELP_PROMPT_TEMPLATE = """
请作为学习辅导老师，给学生输出这道题的解题思路，**不要直接把答案甩出来**，引导学生思考。
【题目题干】：%s
【题目的参考答案】：%s

要求：
1.语言简短易懂，适合学生理解；
2.重点讲解思考步骤，点拨关键点，不能直接完整公布答案；
3.输出中文，格式干净，不要markdown多余符号。
""";


    public Flux<String> streamQuestionSuggest(String questionContent, String referenceAnswer) {
        String fullPrompt = String.format(QUESTION_HELP_PROMPT_TEMPLATE, questionContent, referenceAnswer);
        return chatClient.prompt().user(fullPrompt).stream().content();
    }
}
