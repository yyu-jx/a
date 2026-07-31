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
@Deprecated
public class GenerationMessage {
    private Integer userId;
    private Integer coursewareId;
    private String type; // message | ppt | word | file_info | error
    private Object data;

    /**
     * 创建通用消息
     *
     * @param userId 用户ID
     * @param msg    消息
     * @return 通用消息
     */
    public static GenerationMessage ofMessage(Integer userId, Integer coursewareId, String msg) {
        return new GenerationMessage(userId, coursewareId, "general", msg);
    }

    /**
     * 创建PPT消息
     *
     * @param userId 用户ID
     * @param data   PPT数据
     * @return PPT消息
     */
    public static GenerationMessage ofPpt(Integer userId, Integer coursewareId, PptStatusMessage data) {
        return new GenerationMessage(userId, coursewareId, "ppt", data);
    }

    /**
     * 创建Word消息
     *
     * @param userId 用户ID
     * @param data   Word数据
     * @return Word消息
     */
    @Deprecated
    public static GenerationMessage ofWord(Integer userId, Integer coursewareId, Object data) {
        return new GenerationMessage(userId, coursewareId, "word", data);
    }

    /**
     * 创建文件信息消息
     *
     * @param userId 用户ID
     * @param data   文件信息
     * @return 文件信息消息
     */
    public static GenerationMessage ofFileInfo(Integer userId, Integer coursewareId, Map<String, String> data) {
        return new GenerationMessage(userId, coursewareId, "file_info", data);
    }

    /**
     * 创建错误消息
     *
     * @param userId 用户ID
     * @param data   错误信息
     * @return 错误消息
     */
    @Deprecated
    public static GenerationMessage ofError(Integer userId, Integer coursewareId, Object data) {
        return new GenerationMessage(userId, coursewareId, "error", data);
    }
}
