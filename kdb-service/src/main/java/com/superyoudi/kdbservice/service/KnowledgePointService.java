package com.superyoudi.kdbservice.service;

import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//首页获取知识点信息
@Service
public class KnowledgePointService {

    private final KnowledgePointRepository repository;

    public KnowledgePointService(KnowledgePointRepository repository) {
        this.repository = repository;
    }

    public KnowledgePointDTO getKnowledgePoint(String id) {
        KnowledgePointNode knowledgePointNode = repository.findById(id).orElse(null);
        if (knowledgePointNode == null)
            return null;

        KnowledgePointDTO dto = new KnowledgePointDTO();
        dto.setKnowledgeId(knowledgePointNode.getName());
        dto.setKnowledgeTitle(knowledgePointNode.getDescription());
        return dto;
    }

    /**
     * 批量获取知识点信息
     *
     * @param knowledgeIdList 知识点ID列表
     * @return 知识点信息列表
     */
    public List<KnowledgePointDTO> batchGetKnowledgePoints(List<String> knowledgeIdList) {
        // Repository只允许kdb内部调用！
        List<KnowledgePointNode> nodeList = repository.findAllById(knowledgeIdList);
        // KnowledgePointNode → DTO，隐藏Neo4j内部实体
        return nodeList.stream().map(node -> {
            KnowledgePointDTO dto = new KnowledgePointDTO();
            dto.setKnowledgeId(node.getName());
            dto.setKnowledgeTitle(node.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }
}