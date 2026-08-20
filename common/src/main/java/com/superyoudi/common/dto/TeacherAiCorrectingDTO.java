package com.superyoudi.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherAiCorrectingDTO {
    /** 题干内容 */
    private String questionContent;
    /** 学生作答原文 */
    private String studentAnswerContent;
    /** 参考答案 */
    private String standardAnswer;
    /** 得分关键点列表 */
    private List<String> scoreKeyList;
    /** 本题满分分值 */
    private Integer fullScore;
}