package com.superyoudi.kdbservice.dto;

import java.util.List;

/**
 * 知识图谱返回结构：节点 + 边
 */
@Deprecated
public record KnowledgeGraphDTO(
        List<NodeDTO> nodes,
        List<EdgeDTO> edges) {

    public record NodeDTO(String id, String name, String description) {
    }

    public record EdgeDTO(String source, String target, String type) {
    }
}
