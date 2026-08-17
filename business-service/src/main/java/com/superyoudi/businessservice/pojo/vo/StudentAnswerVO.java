package com.superyoudi.businessservice.pojo.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生单题答题记录 VO
 * <p>answer 字段在数据库中存储为 JSON 字符串，接口层解析为 JSON 对象返回
 *
 * @author at
 */
@Data
public class StudentAnswerVO {

    /** 答题记录唯一主键ID */
    private Long id;

    /** 所属提交ID（关联任务提交记录） */
    private Long submissionId;

    /** 题目ID */
    private Long questionId;

    /** 学生答案（不同题型结构不同，JSON 对象） */
    private JsonNode answer;

    /** 单题得分 */
    private Integer score;

    /** 评语 */
    private String comment;

    /** 耗时（秒） */
    private Integer timeSpent;

    /** 提交前的修改次数 */
    private Integer attemptCount;

    /** 求助次数 */
    private Integer helpRequestCount;

    /** 错误类型（null表示无错误） */
    private String errorType;

    /** 创建时间（答题时间） */
    private LocalDateTime createdAt;
}
