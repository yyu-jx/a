package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class ChatHistoryDTO {
    private Long id;
    private Long userId;
    private List<ChatMessageData> messages;

    @Data
    @AllArgsConstructor
    public static class ChatMessageData {
        private Long id;
        private Integer msgSeq;
        private String msgType;
        private String message;
        private String role;
        private LocalDateTime sentAt;
    }
}
