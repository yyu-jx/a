package com.superyoudi.aiservice.ai.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Sijayyx
 */
@Slf4j
@Component
public class AnswerCorrectionModel {

    private final ChatClient chatClient;

    public AnswerCorrectionModel(ChatClient chatClient) {
        this.chatClient = chatClient;

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
    public CorrectionResult doFillAnswerCorrection(int totalScore, String question, List<FillAnswerEntry> entries) throws RuntimeException {
        try {
            String entriesJson = AiMessageUtils.LENIENT_MAPPER.writeValueAsString(entries);

            String prompt = String.format("""
                    你是一名作业批改助手。请根据以下信息，对填空题进行评分，并严格按照以下JSON格式返回结果，不附加任何其他内容。
                    
                    {
                        "score": 整数,           // 评分
                        "errorType": "字符串",    // 错误类型
                        "comment": "字符串"      // 评语
                    }
                    
                    题干：
                    %s
                    
                    正确答案与学生答案列表（index为题号，refAnswer为正确答案，studentAnswer为学生答案）：
                    %s
                    
                    题目总分：%d
                    
                    评分规则：
                    1. 每道小题分值 = 题目总分 / 填空题空数（若无法整除，按整数除法取整，剩余分值由最后一题承担）。
                    2. 若学生答案与正确答案意思一致，则该小题得满分，否则得0分。
                    3. 汇总所有小题得分，作为最终题目得分（score）。
                    4. errorType 字段从以下枚举值中选择其一：
                       - "concept_error"     ：概念理解错误
                       - "method_error"      ：解题方法错误
                       - "calc_error"        ：计算错误
                       - "misread_question"  ：审题错误
                       - "other"             ：其它
                       若所有小题全部正确（score == 题目总分），则 errorType 设为 null。
                    5. comment 中简要说明评分理由，不包含任何多余信息。
                    6. 最终只返回一个合法的JSON对象，不包含解释或额外文本。
                    """, question, entriesJson, totalScore);

            String result = chatClient.prompt(prompt).call().content();
            String json = AiMessageUtils.extractJson(result);
            return AiMessageUtils.convertJsonValue(json, CorrectionResult.class);

        } catch (JsonProcessingException e) {
            log.error("Json转换失败", e);
            throw new RuntimeException(e);
        }
    }

    public CorrectionResult doSubjectiveAnswerCorrection(int totalScore, String question, String questionAnswer, String studentAnswer) throws RuntimeException {
        String prompt = String.format("""
                你是一名作业批改助手。请根据以下信息，对主观题进行评分，并严格按照以下JSON格式返回结果，不附加任何其他内容。
                
                {
                    "score": 整数评分,
                    "errorType": "错误类型",
                    "comment": "评语"
                }
                
                题干：
                %s
                
                正确答案：
                %s
                
                学生答案：
                %s
                
                题目总分：%d
                
                评分规则：
                1. 若学生答案与正确答案意思一致，则该题得满分（score = 题目总分）。
                2. 若学生答案与正确答案完全不一致，则该题得0分（score = 0）。
                3. 若部分一致，则给部分分（score = 相应分值）。
                4. errorType 字段从以下枚举值中选择其一：
                   - "concept_error"  ：概念理解错误
                   - "method_error"   ：解题方法错误
                   - "calc_error"     ：计算错误
                   - "misread_question"：审题错误
                   - "other"          ：其它
                   如果答案完全正确（得满分），则 errorType 设为 null。
                5. comment 中简要说明评分理由，不包含任何多余信息。
                6. 最终只返回一个合法的JSON对象，不包含解释或额外文本。
                """, question, questionAnswer, studentAnswer, totalScore);

        String result = chatClient.prompt(prompt).call().content();
        String json = AiMessageUtils.extractJson(result);
        return AiMessageUtils.convertJsonValue(json, CorrectionResult.class);
    }

    @Data
    @AllArgsConstructor
    public static class CorrectionResult {
        private int score; // 评分
        private String errorType; // 错误类型
        private String comment; // 评语
    }

    @AllArgsConstructor
    @Data
    public static class FillAnswerEntry {
        private int index;
        private String refAnswer;
        private String studentAnswer;
    }
}
