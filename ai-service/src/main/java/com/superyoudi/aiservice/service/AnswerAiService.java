package com.superyoudi.aiservice.service;

import com.superyoudi.common.dto.TeacherAiCorrectingDTO;
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

    public Flux<String> getCorrectionSuggestions(TeacherAiCorrectingDTO dto) {
        String prompt = buildPrompt(dto);
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
    /**
     * 组装批改提示词
     */
    private String buildPrompt(TeacherAiCorrectingDTO dto) {

        return "你是教学批改助手，针对学生主观题作答给出批改建议。\n" +
                "【题干】：" + dto.getQuestionContent() + "\n" +
                "【参考答案】：" + dto.getStandardAnswer() + "\n" +
                "【得分关键点】：" + dto.getScoreKeyList() + "\n" +
                "【学生作答】：" + dto.getStudentAnswerContent() + "\n" +
                "\n要求：" +
                "\n1.对照得分关键点，分析学生哪些关键点答出来，哪些缺失；" +
                "\n2.给出简短清晰批改建议，不要输出多余markdown格式；" +
                "\n3.不要直接给出完整标准答案，重在点评学生答案。";
    }
}
