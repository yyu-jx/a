package com.superyoudi.coursewareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 课件生成状态，发送给前端展示。
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class CoursewareGenState {
    public static final String STATUS_GENERATING = "generating";
    public static final String STATUS_SUCCESS = "success";

    private Integer userId;
    private Integer coursewareId;
    private String status; // 生成状态 generating | success

    private String pptHtml;
    private String wordHtml;
}
