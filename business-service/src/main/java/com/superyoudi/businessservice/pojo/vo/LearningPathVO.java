package com.superyoudi.businessservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPathVO {
    private Long pathId;
    private Long courseId;
    private String additionalInfo;
    private List<PathNode> nodeList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PathNode {
        private Long nodeId;
        private String nodeName;
        private String knowledgeIds; // JSON
        private String status;
    }

}
