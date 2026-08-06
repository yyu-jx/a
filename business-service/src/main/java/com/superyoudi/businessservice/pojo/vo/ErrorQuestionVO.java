package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErrorQuestionVO {
    /** 题目id */
    private Integer questionId;
    /** 题目类型 choice/fill/subjective/true_false */
    private String questionType;
    /** 题目内容 */
    private String questionContent;
    /** 错误类型，null代表没错 */
    private String errorType;
    /** 来源任务id（作业id） */
    private Integer sourceTaskId;
    /** 来源任务名称 */
    private String sourceTaskName;
    /** 学生这道题得分 */
    private BigDecimal studentScore;
    /** 题目总分 */
    private BigDecimal fullScore;
    /** 错误作答时间 */
    private LocalDateTime wrongTime;
}