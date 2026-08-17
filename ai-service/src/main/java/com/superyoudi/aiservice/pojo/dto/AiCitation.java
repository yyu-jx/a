package com.superyoudi.aiservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引用表 ai_citations
 * AI回答/AI建议附带引用来源；chatMessageId、suggestionsId二选一赋值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AiCitation {

    public static final String DATA_TYPE_FILE = "file";
    public static final String DATA_TYPE_URL = "url";
    public static final String DATA_TYPE_TEXT_QUOTE = "text_quote";
    public static final String DATA_TYPE_STUDENT_RECORD = "student_record";
    public static final String DATA_TYPE_STUDENT_PROGRESS = "student_progress";
    public static final String DATA_TYPE_KNOWLEDGE = "knowledge";
    public static final String DATA_TYPE_QUESTION = "question";
    public static final String DATA_TYPE_NONE = "none";

    /**
     * 主键id
     */
    private Long id;
    /**
     * 关联对话消息id，与suggestionsId二选一
     */
    private Long chatMessageId;
    /**
     * 关联AI建议id，与chatMessageId二选一
     */
    private Long suggestionsId;
    /**
     * 引用标题，前端展示文本，例：基于近5次作业分析
     */
    private String title;
    /**
     * 引用数据类型，控制前端点击行为
     * file文件｜url链接｜text_quote文本引用｜student_record答题记录
     * student_progress学习进度｜knowledge知识点｜question题库题目｜exam试卷
     */
    private String dataType;
    /**
     * 引用附加数据，JSON格式字符串
     */
    private String data;


    /// URL引用
    @Data
    @AllArgsConstructor
    public static class Url {
        /// 网站地址
        private String url;
        /// 网站名称
        private String siteName;
        /// 引用的内容或描述
        private String description;

    }

    /// 知识点引用
    @Data
    @AllArgsConstructor
    public static class KnowledgePoint {
        /// 知识点名称
        private String name;
        /// 知识点说明
        private String description;
    }

    /// 文本引用
    @Data
    @AllArgsConstructor
    public static class TextQuote {

        /// 引用的文本片段
        private String text;
        /// 来源
        private String source;
    }
}