package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话元数据表 chat_session
 * 用于教师端AI智教对话会话基础信息
 */
@Data
@AllArgsConstructor
public class ChatSession {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 对应用户id
     */
    private Long userId;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}