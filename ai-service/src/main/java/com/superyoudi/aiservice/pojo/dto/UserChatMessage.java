package com.superyoudi.aiservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户向AI发送的聊天消息
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChatMessage<T> {

    /// 用户ID
    private Long userId;

    /// 聊天消息（文本）
    private String message;

    /// 附加的结构化数据
    private T additionalData;
}
