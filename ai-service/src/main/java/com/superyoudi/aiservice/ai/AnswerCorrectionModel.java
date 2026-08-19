package com.superyoudi.aiservice.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Sijayyx
 */
@Component
public class AnswerCorrectionModel {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AnswerCorrectionModel(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用模型批改填空题
     *
     * @param totalScore 题目总分
     * @param question   题干
     * @param entries    答案列表
     * @return 评分结果
     * @throws RuntimeException 调用模型失败/Json转换失败
     */
    public int doFillAnswerCorrection(int totalScore, String question, List<FillAnswerEntry> entries) throws RuntimeException {
        try {
            String entriesJson = objectMapper.writeValueAsString(entries);

            String prompt = String.format("""
                    你是一名作业批改助手。请根据以下信息，对填空题进行评分，并仅返回一个整数（题目得分），不附加任何其他内容。
                    
                    题干：
                    %s
                    
                    正确答案与学生答案列表（index为题号，refAnswer为正确答案，studentAnswer为学生答案）：
                    %s
                    
                    题目总分：%d
                    
                    评分规则:
                    - 每道小题分值 = 题目总分 / 填空题空数（若无法整除，按整数除法取整，剩余分值由最后一题承担）。
                    - 若学生答案与正确答案意思一致，则该小题得满分，否则得0分。若学生答案与正确答案完全不一致，则该小题得0分。
                    - 汇总所有小题得分，作为最终题目得分。
                    - 最终只返回一个整数得分，不包含解释或额外文本。
                    """, question, entriesJson, totalScore);

            String score = chatClient.prompt(prompt).call().content();
            if (score != null) {
                return Integer.parseInt(score);
            } else
                throw new RuntimeException("题目批改失败");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public int doSubjectiveAnswerCorrection(int totalScore, String question, String questionAnswer, String studentAnswer) throws RuntimeException {
        String prompt = String.format("""
                你是一名作业批改助手。请根据以下信息，对主观题进行评分，并仅返回一个整数（题目得分），不附加任何其他内容。
                
                题干：
                %s
                
                正确答案:
                %s
                
                学生答案:
                %s
                
                题目总分：%d
                
                评分规则:
                - 若学生答案与正确答案意思一致，则该题得满分。若学生答案与正确答案完全不一致，则该题得0分。若部分一致，则给部分分。
                - 最终只返回一个整数得分，不包含解释或额外文本。
                """, question, questionAnswer, studentAnswer, totalScore);

        String score = chatClient.prompt(prompt).call().content();
        if (score != null) {
            return Integer.parseInt(score);
        } else
            throw new RuntimeException("题目批改失败");
    }


    @AllArgsConstructor
    @Data
    public static class FillAnswerEntry {
        private int index;
        private String refAnswer;
        private String studentAnswer;
    }
}
