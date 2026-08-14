package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题目实体
 * 对应数据库 questions 题目表
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Question {


    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 布置教师的用户ID
     */
    private Long teacherId;

    /**
     * 题型
     * choice 选择题 | fill 填空题 | subjective 主观题 | true_false 判断题
     */
    private String type;

    /**
     * 题干
     */
    private String content;

    /**
     * 答案，JSON文本字符串
     */
    private String answer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;
}