package com.superyoudi.kdbservice.tools.impl;

import com.superyoudi.aiservice.ai.tools.RagTools;
import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.common.constants.AgentConstants;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Slf4j
@Component
public class RagToolsImpl implements RagTools {
    private final KnowledgeGraphService knowledgeGraphService;

    public RagToolsImpl(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @Override
    @Tool(name = "query_knowledge_graph", description = "检索知识图谱，查找相关的文档和附带关系的知识点集合。")
    public String queryKnowledgeGraph(@ToolParam(description = "检索知识图谱的查询文本。") String query) {
        return knowledgeGraphService.retrieveKnowledgeGraph(query);
    }

    @Override
    @Tool(name = "query_knowledge_point", description = "检索知识图谱，查找特定的知识点及其关系。")
    public String queryKnowledgePoint(@ToolParam(description = "知识点名称") String knowledgeName) {
        return knowledgeGraphService.retrieveKnowledgePoint(knowledgeName);
    }

    @Tool(name = "add_citation", description = "为消息添加引用信息。")
    @Override
    @SuppressWarnings("unchecked")
    public String addCitationToMessage(
            @ToolParam(description = """
                    引用标题，例如: "知识点: 导数", "基于有关xxx的文本分析"
                    """) String title,
            @ToolParam(description = """
                    引用类型, 可选的有:
                    knowledge, 知识点引用
                    text_quote, 文本引用
                    """) String type,
            @ToolParam(description = """
                    引用信息的JSON格式，请按照对应的类型填入:
                    
                    知识点引用 knowledge:
                    {
                        "name": "知识点名称",
                        "description": "知识点描述"
                    }
                    
                    文本引用 text_quote:
                    {
                        "text": "引用的文本片段",
                        "source": "文本来源"
                    }
                    """) String json,
            // 工具上下文
            ToolContext toolContext) {

        Map<String, Object> metadata = (Map<String, Object>) toolContext.getContext().get(AgentConstants.METADATA_CUSTOM_METADATA);

        if (metadata == null) {
            log.error("错误: 工具上下文中没有自定义元数据。");
            return "错误: 无法添加引用信息，请跳过执行。";
        }

        if (!metadata.containsKey(AgentConstants.CUSTOM_METADATA_CITATIONS))
            metadata.put(AgentConstants.CUSTOM_METADATA_CITATIONS, new ArrayList<AiCitation>());

        AiCitation citation = AiCitation.builder()
                .title(title)
                .dataType(type)
                .data(json)
                .build();

        boolean added = ((ArrayList<AiCitation>) metadata.get(AgentConstants.CUSTOM_METADATA_CITATIONS)).add(citation);

        log.info("添加引用信息: {}", added ? "添加成功" : "添加失败");

        return "执行成功。";
    }
}
