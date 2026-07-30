package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话‑消息表 chat_messages
 * AI对话消息记录，AI也作为发送方，依靠msgSeq做会话内排序
 */
@Data
public class ChatMessage {
    /** 主键id */
    private Long id;
    /** 会话id，关联chat_session.id */
    private Long chatId;
    /** 会话内消息递增序列号，用于排序 */
    private Integer msgSeq;
    /** 消息类型：text文本｜media媒体｜file文件｜system系统消息 */
    private String msgType;
    /** 发送者用户id，AI也映射为虚拟用户id */
    private Long senderId;
    /** 消息内容，支持JSON字符串 / URL地址 */
    private String message;
    /** 消息发送时间 */
    private LocalDateTime sentAt;
}