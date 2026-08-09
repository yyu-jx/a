package com.superyoudi.aiservice.pojo.dto;

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
public class LearningPathDataDTO {
    private Long studentId;
    private Long courseId;
    private String additionalInfo; // AI对于此学习路径节点的描述，包括主要的内容，重点知识等。
    private List<PathNodeData> pathNodeDataList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PathNodeData {
        private String nodeName;
        private List<String> knowledgeIds;
    }
}
