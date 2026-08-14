package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目属性表
 *
 * @author Sijayyx
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionProperty {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的题目ID
     */
    private Long questionId;

    /**
     * 解析（可选）
     */
    private String analysis;

    /**
     * 题目来源：ai | manual | import
     */
    private String source;

    /**
     * 难度：easy | medium | hard
     */
    private String difficulty;

}