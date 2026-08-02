package com.superyoudi.coursewareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * 课件生成状态，发送给前端展示。
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class GenerationStatus {
    public static final String STATUS_GENERATING = "generating";
    public static final String STATUS_SUCCESS = "success";

    private Long coursewareId;
    private String status; // 生成状态 generating | success

    private String pptHtml;
    private String wordHtml;

    public static GenerationStatus generatingPpt(Long coursewareId, @NotNull String pptHtml) {
        return new GenerationStatus(coursewareId, STATUS_GENERATING, pptHtml, "");
    }

    public static GenerationStatus generatingWord(Long coursewareId, @NotNull String wordHtml) {
        return new GenerationStatus(coursewareId, STATUS_GENERATING, "", wordHtml);
    }

    public static GenerationStatus success(Long coursewareId, @Nullable String pptHtml, @Nullable String wordHtml) {
        return new GenerationStatus(coursewareId, STATUS_SUCCESS, pptHtml, wordHtml);
    }
}
