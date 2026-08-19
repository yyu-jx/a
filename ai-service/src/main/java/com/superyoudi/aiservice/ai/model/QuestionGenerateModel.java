package com.superyoudi.aiservice.ai.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Sijayyx
 */
@Component
public class QuestionGenerateModel {
    private final ChatClient chatClient;

    public QuestionGenerateModel(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @NotNull
    private static String getQuestionTypeJson(String questionType) {
        return switch (questionType) {
            case "choice" -> """
                    {
                        "correct": ["A", "C"], // 若为单选，则此行为 "select":"A"
                        "options": [
                            {"key": "A", "text": "内容1"},
                            {"key": "B", "text": "内容2"}
                        ]
                    }
                    """;
            case "fill" -> """
                    {
                        "blanks": [
                            {"index": "1", "answer": "答案"},
                            {"index": "2", "answer": "答案"}
                        ]
                    }
                    """;
            case "subjective" -> """
                    {
                        "answer": "先求一阶导找驻点，二阶导判别极值",
                        "score_key": ["求驻点得3分", "二阶导数判断得2分"]
                    }
                    """;
            case "true_false" -> """
                    {
                        "result": "true"
                    }
                    """;
            default -> throw new IllegalArgumentException("非法的题目类型: " + questionType);
        };
    }

    public QuestionGenerateResult call(List<String> knowledge, String questionType, int count) {
        String questionTypeJson = getQuestionTypeJson(questionType);

        String prompt = String.format("""
                请根据以下知识库和题目类型，生成%d道题目，题目类型为%s，题目需要关联如下知识点：%s
                
                请返回以下格式的JSON数据，不要输出其它内容:
                 {
                   "questionData": [
                     {
                       "content": "题干内容",
                       "answer": %s
                     }
                   ]
                 }
                """, count, questionType, String.join(", ", knowledge), questionTypeJson);

        String content = chatClient.prompt(prompt).call().content();

        String json = AiMessageUtils.extractJson(content);
        return AiMessageUtils.convertJsonValue(json, QuestionGenerateResult.class);
    }

    @Data
    @AllArgsConstructor
    public static class QuestionGenerateResult {
        List<QuestionData> questionData;

        @Data
        @AllArgsConstructor
        public static class QuestionData {
            private String content;
            private JsonNode answer; // JSON
        }
    }
}
