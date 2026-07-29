package com.superyoudi.coursewareservice.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * websocket通用消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationMessage {
    private Integer userId;
    private String type; // message | ppt | word | file_info | error
    private Object data;

    /**
     * 创建通用消息
     *
     * @param userId 用户ID
     * @param msg    消息
     * @return 通用消息
     */
    public static GenerationMessage ofMessage(Integer userId, String msg) {
        return new GenerationMessage(userId, "general", msg);
    }

    /**
     * 创建PPT消息
     *
     * @param userId 用户ID
     * @param data   PPT数据
     * @return PPT消息
     */
    public static GenerationMessage ofPpt(Integer userId, PptStatusMessage data) {
        return new GenerationMessage(userId, "ppt", data);
    }

    /**
     * 创建Word消息
     *
     * @param userId 用户ID
     * @param data   Word数据
     * @return Word消息
     */
    @Deprecated
    public static GenerationMessage ofWord(Integer userId, Object data) {
        return new GenerationMessage(userId, "word", data);
    }

    /**
     * 创建文件信息消息
     *
     * @param userId 用户ID
     * @param data   文件信息
     * @return 文件信息消息
     */
    public static GenerationMessage ofFileInfo(Integer userId, Map<String, String> data) {
        return new GenerationMessage(userId, "file_info", data);
    }

    /**
     * 创建错误消息
     *
     * @param userId 用户ID
     * @param data   错误信息
     * @return 错误消息
     */
    @Deprecated
    public static GenerationMessage ofError(Integer userId, Object data) {
        return new GenerationMessage(userId, "error", data);
    }
}
