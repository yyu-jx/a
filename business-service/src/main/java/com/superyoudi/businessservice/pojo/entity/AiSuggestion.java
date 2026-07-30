package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI建议表 ai_suggestions
 * 统一存放AI生成界面内嵌建议，根据displayLocation控制前端展示位置
 */
@Data
public class AiSuggestion {
    /** 主键id */
    private Long id;
    /** 接收该建议的用户id */
    private Long receiverId;
    /** 建议标题，可为空 */
    private String title;
    /** 建议正文，markdown格式 */
    private String message;
    /** 显示样式：info普通提示｜warning警告提示 */
    private String displayType;
    /** 前端展示位置标识 */
    private String displayLocation;
    /** 记录创建时间 */
    private LocalDateTime createdAt;
}