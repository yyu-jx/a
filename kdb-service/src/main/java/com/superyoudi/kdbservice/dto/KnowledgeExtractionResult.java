package com.superyoudi.kdbservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Sijayyx
 * @date 2026/7/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeExtractionResult {

    private List<KnowledgePointItem> knowledgePoints;
    private List<KnowledgeRelationItem> knowledgeRelations;

    @Data
    public static class KnowledgePointItem {
        private String name;
        private String description;
    }

    @Data
    public static class KnowledgeRelationItem {
        private String relation;
        private String sourceKnowledgePoint;
        private String targetKnowledgePoint;
    }
}
