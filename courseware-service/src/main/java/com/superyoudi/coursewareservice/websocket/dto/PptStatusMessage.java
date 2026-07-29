package com.superyoudi.coursewareservice.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PPT状态消息，用于向前端推送
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PptStatusMessage {
    private String status; // start | generating | editing | done
    private Integer slideId; // [0, n], 非生成/修改时标记为-1, start时为总页数
    private String info; // 信息

    public static PptStatusMessage ofStart(Integer slideCount, String info) {
        return new PptStatusMessage("start", slideCount, info);
    }

    public static PptStatusMessage ofGenerating(Integer slideId, String info) {
        return new PptStatusMessage("generating", slideId, info);
    }

    public static PptStatusMessage ofEditing(Integer slideId, String info) {
        return new PptStatusMessage("editing", slideId, info);
    }

    public static PptStatusMessage ofDone(String info) {
        return new PptStatusMessage("done", null, info);
    }
}
