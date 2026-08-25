package com.superyoudi.kdbservice.controller;

import com.superyoudi.common.entity.Result;
import com.superyoudi.kdbservice.dto.KnowledgeGraphDTO;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/kdb")
class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    /**
     * 获取全部知识图谱（节点 + 边），供前端可视化
     */
    @Deprecated
    @GetMapping("/graph")
    public Result<KnowledgeGraphDTO> graph() {
        return Result.success(knowledgeGraphService.getKnowledgeGraph());
    }


}
