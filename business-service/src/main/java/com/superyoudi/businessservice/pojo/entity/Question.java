package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceAnswer {

        public static final String SCHEMA_SINGLE = """
                {
                    "select":"A",
                    "options": [
                        {"key": "A", "text": "内容1"},
                        {"key": "B", "text": "内容2"}
                    ]
                }
                """;

        public static final String SCHEMA_MULTI = """
                //多选
                """;

        /**
         * 选项列表
         */
        private List<Option> options;

        /**
         * 正确答案（单选时使用，如 "A"）
         */
        private String select;

        /**
         * 正确答案列表（多选时使用，如 ["A", "C"]）
         */
        private List<String> correct;

        /**
         * 选项类
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Option {
            /**
             * 选项键：A, B, C, D
             */
            private String key;

            /**
             * 选项文本内容
             */
            private String text;
        }
    }

    /**
     * 填空题答案
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FillAnswer {

        public static final String SCHEMA = """
                {
                    "blanks": [
                        {"index": "1", "answer": "答案"},
                        {"index": "2", "answer": "答案"}
                    ]
                }
                """;
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
             * 正确答案
             */
            private String answer;
        }
    }

    /**
     * 主观题答案
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectiveAnswer {

        public static final String SCHEMA = """
                {
                    "answer": "先求一阶导找驻点，二阶导判别极值",
                    "score_key": ["求驻点得3分", "二阶导数判断得2分"]
                }
                """;

        /**
         * 参考答案
         */
        private String answer;

        /**
         * 评分要点列表
         */
        private List<String> scoreKey;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrueFalseAnswer {

        public static final String SCHEMA = """
                {
                    "result": "true"
                }
                """;

        /**
         * 判断结果：true 正确，false 错误
         */
        private Boolean result;
    }
}