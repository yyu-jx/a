package com.superyoudi.coursewareservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoursewareFileRel {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课件记录ID
     */
    private Long coursewareRecordId;

    /**
     * 文件ID
     */
    private Long fileId;

}
