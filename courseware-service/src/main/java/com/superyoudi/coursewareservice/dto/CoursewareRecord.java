package com.superyoudi.coursewareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课件生成记录实体类
 *
 * @author Sijayyx
 * @date 2026-08-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursewareRecord {

    private Long id;
    private Long userId;
    private Long chatId;
    private Long pptFileId;
    private Long wordFileId;
    private String pptHtml;
    private String wordHtml;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}