package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class SectionKnowledgeRel {
    private Long id;
    private Long sectionId;
    private String neo4jKpId;
    private LocalDateTime createdTime;

}
