package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生单题答题实体
 * 对应数据库 student_answers 学生单题答题表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentAnswer {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属的提交ID
     */
    private Long submissionId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 学生答案，JSON字符串
     */
    private String answer;

    /**
     * 单题得分
     */
    private Integer score;

    /**
     * 单题教师评语
     */
    private String comment;

    /**
     * 答题耗时，单位秒
     */
    private Integer timeSpent;

    /**
     * 提交前的修改次数
     */
    private Integer attemptCount;

    /**
     * 求助次数
     */
    private Integer helpRequestCount;

    /**
     * 错误类型，null代表无错误
     */
    private String errorType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FillAnswer {

        /**
         * 填空列表
         */
        private List<Blank> blanks;

        /**
         * 填空项
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Blank {
            /**
             * 填空序号
             */
            private String index;

            /**
             * 学生填写的内容
             */
            private String input;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectiveAnswer {

        /**
         * 学生回答内容
         */
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrueFalseAnswer {

        /**
         * 判断选择：true 正确，false 错误
         */
        private Boolean choose;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceAnswer {

        /**
         * 单选答案（如 "A"）
         */
        private String select;

        /**
         * 多选答案列表（如 ["A", "C"]）
         * 当为多选时使用，单选时此字段为null
         */
        private List<String> selects;
    }
}