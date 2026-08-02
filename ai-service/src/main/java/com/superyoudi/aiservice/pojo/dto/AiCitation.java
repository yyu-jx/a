package com.superyoudi.aiservice.pojo.dto;

import lombok.Data;

/**
 * AI引用表 ai_citations
 * AI回答/AI建议附带引用来源；chatMessageId、suggestionsId二选一赋值
 */
@Data
public class AiCitation {
    /** 主键id */
    private Long id;
    /** 关联对话消息id，与suggestionsId二选一 */
    private Long chatMessageId;
    /** 关联AI建议id，与chatMessageId二选一 */
    private Long suggestionsId;
    /** 引用标题，前端展示文本，例：基于近5次作业分析 */
    private String title;
    /**
     * 引用数据类型，控制前端点击行为
     * file文件｜url链接｜text_quote文本引用｜student_record答题记录
     * student_progress学习进度｜knowledge知识点｜exercise题库题目｜exam试卷
     */
    private String dataType;
    /** 引用附加数据，JSON格式字符串 */
    private String data;
}