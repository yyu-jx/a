package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话‑消息表 chat_messages
 * AI对话消息记录，AI也作为发送方，依靠msgSeq做会话内排序
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    public final static String MSG_TYPE_TEXT = "text";
    public final static String MSG_TYPE_MEDIA = "media";
    public final static String MSG_TYPE_FILE = "file";

    public final static String ROLE_AI = "ai";
    public final static String ROLE_USER = "user";
    public final static String ROLE_SYSTEM = "system";

    /**
     * 主键id
     */
    private Long id;
    /**
     * 会话id，关联chat_session.id
     */
    private Long chatId;
    /**
     * 会话内消息递增序列号，用于排序
     */
    private Integer msgSeq;
    /**
     * 消息类型：text文本｜media媒体｜file文件
     */
    private String msgType;
    /**
     * 发送者角色 ai | user | system
     */
    private String role;
    /**
     * 消息内容，支持JSON字符串 / URL地址
     */
    private String message;
    /**
     * 消息发送时间
     */
    private LocalDateTime sentAt;
}