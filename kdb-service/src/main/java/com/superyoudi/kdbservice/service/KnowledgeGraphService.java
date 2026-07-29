package com.superyoudi.kdbservice.service;

import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.graph.KnowledgePointRelation;
import com.superyoudi.kdbservice.graph.KnowledgeTextFragmentNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import com.superyoudi.kdbservice.repository.KnowledgeTextFragmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class KnowledgeGraphService {
    /**
     * 宽松的 JSON 解析器，容忍小模型输出的格式问题
     */
    private static final ObjectMapper LENIENT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)  // 允许无引号字段名
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)         // 允许单引号
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)        // 允许尾随逗号
            .build()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    private final ChatClient chatClient;

    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgeTextFragmentRepository knowledgeTextFragmentRepository;
    private final VectorStore vectorStore;

    public KnowledgeGraphService(ChatClient.Builder chatClient, KnowledgePointRepository knowledgePointRepository,
                                 KnowledgeTextFragmentRepository knowledgeTextFragmentRepository, VectorStore vectorStore) {

        log.info("KnowledgeGraphService 初始化。");

        this.chatClient = chatClient.defaultSystem("""
                # 角色设定
                你是一位知识图谱构建专家，擅长从技术文档、教材或学术文本中提取结构化的知识点及其关系。
                
                # 任务目标
                请从用户提供的文本中，提取所有重要的知识点（概念、术语、方法、理论等），并识别它们之间的语义关系。输出结果必须严格遵循指定的JSON格式。
                
                # 知识点抽取规则
                1. **知识点粒度**：抽取原子性的、独立可理解的知识单元。避免抽取过于宽泛（如“数学”）或过于琐碎（如“这是一个例子”）的短语。
                2. **知识点命名**：使用**标准术语**，长度控制在2-10个汉字或英文单词。
                3. **知识点描述**：用简洁且准确的话描述该知识点的核心含义，相当于一个简短的百科解释。
                
                # 关系类型定义（严格遵循以下规范）
                请从以下预定义关系类型中选择最匹配的一项。**优先使用前三种（IS_A, HAS_A, PREREQUISITE_OF）**。
                
                | **关系类型** | **含义** | **方向** | **示例** |
                | --- | --- | --- | --- |
                | `IS_A` | 子类/实例关系 | 有向 | `(快速排序)-[:IS_A]->(排序算法)` |
                | `HAS_A` | 组成/包含关系 | 有向 | `(CPU)-[:HAS_A]->(寄存器)` |
                | `PREREQUISITE_OF` | 前置依赖关系 | 有向 | `(极限)-[:PREREQUISITE_OF]->(导数)` |
                | `APPLIED_IN` | 应用/实践关系 | 有向 | `(贝叶斯定理)-[:APPLIED_IN]->(垃圾邮件过滤)` |
                | `SIMILAR_TO` | 相似/类比关系 | **无向** | `(栈)-[:SIMILAR_TO]-(队列)` |
                | `DIFFERENTIATES_FROM` | 区分/对比关系 | 有向 | `(数组)-[:DIFFERENTIATES_FROM]->(链表)` |
                | `RELATED_TO` | 一般关联关系（**无合适关系时使用**） | **无向** | `(量子力学)-[:RELATED_TO]-(相对论)` |
                
                ### 关系方向说明
                - **有向**：关系有明确的起点和终点，例如 `A` 是 `B` 的一种（A -> B）。
                - **无向**：关系是相互的，例如 `A` 和 `B` 是相似的（A - B）。在JSON中，`sourceKnowledgePoint` 和 `targetKnowledgePoint` 仍需要填写，但顺序不重要。
                
                # 输出格式要求
                - 返回一个**合法的JSON对象**，不要包含任何额外的解释、Markdown标记（如 ```json）或注释。
                - JSON结构必须与以下模型完全匹配：
                
                ```json
                {
                  "knowledgePoints": [
                    {
                      "name": "知识点名称",
                      "description": "简洁的1-2句描述"
                    }
                  ],
                  "knowledgeRelations": [
                    {
                      "relation": "关系类型（如 IS_A）",
                      "sourceKnowledgePoint": "源知识点名称（必须与 knowledgePoints 中的 name 完全一致）",
                      "targetKnowledgePoint": "目标知识点名称（必须与 knowledgePoints 中的 name 完全一致）"
                    }
                  ]
                }
                
                ### 抽取注意事项
                - knowledgeRelations 中的知识点必须要在 knowledgePoints 中存在。
                - 仅抽取显式或强隐含的关系，不要过度推断。
                - 知识点名称必须精确匹配：knowledgeRelations 中的 sourceKnowledgePoint 和 targetKnowledgePoint 必须与 knowledgePoints 中的 name 字段完全一致（大小写、空格敏感）。
                - 如果无强关系，使用 RELATED_TO。
                - 每个字段名必须用双引号包裹，如 "name"、"type"
                - 每个字符串值必须用双引号包裹
                - 对象内每个属性格式为 "key": "value"，注意冒号
                - 数组最后一个元素后不要加逗号
                - 如果没有实体或关系，对应数组为空：[]
                """).build();
        this.knowledgePointRepository = knowledgePointRepository;
        this.knowledgeTextFragmentRepository = knowledgeTextFragmentRepository;
        this.vectorStore = vectorStore;

    }

    public void extractKnowledgePoints(String originalText) {
        Document document = new Document(originalText);

        SentenceSplitter sentenceSplitter = new SentenceSplitter();

        // 拆分原始文本块，避免超出token限制
        List<Document> documents = sentenceSplitter.split(document);

        for (Document doc : documents) {
            if (doc.getText() == null || doc.getText().isBlank())
                continue;

            String text = doc.getText();

            String contentJson = chatClient.prompt(String.format("""
                    请抽取如下文本的知识点与关系:
                    %s
                    """, text)).call().content();

            // 清洗Json
            contentJson = extractJson(contentJson);

            log.info("抽取结果:\n{}", contentJson);

            try {
                KnowledgeExtractionResult result = LENIENT_MAPPER.readValue(contentJson, KnowledgeExtractionResult.class);
                saveKnowledgePoints(result, doc);
            } catch (Exception e) {
                log.error("解析失败", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据查询文本的语义获取相关知识点关系和资料
     *
     * @param queryText 查询文本
     * @return 提示词
     */
    public String retrieveDocumentKnowledgePoints(String queryText) {

        // 查询知识点
        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(queryText)
                .similarityThreshold(0.7)
                .topK(2)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        Set<KnowledgePointNode> knowledgePointNodes = new HashSet<>();
        documents.forEach(doc -> {
            String fragmentId = (String) doc.getMetadata().get("fragment_id");
            knowledgeTextFragmentRepository.findById(fragmentId)
                    .ifPresent(fragment -> knowledgePointNodes.addAll(fragment.getKnowledgePointNodes()));
        });

        /*
        提示词格式:

        ---
        知识点及关系:
        1. XXX -[关系]-> YYY
        2. XXX -[关系]-> YYY
        3. XXX -[关系]-> YYY

        相关资料:
        1. xxxxxxxxx
        2. xxxxxxxxx
        ---
         */

        int num = 1;
        StringBuilder prompt = new StringBuilder();
        prompt.append("---\n");
        prompt.append("知识点及关系:\n");
        for (KnowledgePointNode kp : knowledgePointNodes) {
            for (KnowledgePointRelation relatedKp : kp.getRelatedToKnowledgePoints()) {
                prompt.append(num).append(". ").append(kp.getName())
                        .append(" -[").append(relatedKp.getRelationType()).append("]-> ")
                        .append(relatedKp.getTargetNode().getName()).append("\n");
                num++; // 因为要计算序号就不能用forEach了...
            }
        }

        num = 1;

        prompt.append("\n");
        prompt.append("相关资料:\n");
        for (Document doc : documents) {
            prompt.append(num).append(". ").append(doc.getText()).append("\n");
        }
        prompt.append("---\n");

        return prompt.toString();
    }

    /**
     * 查询知识点注释并列出与其相关的知识点
     *
     * @param knowledgeName 知识点名称
     * @return 提示词
     */
    public String retrieveKnowledgePointInfo(String knowledgeName) {
        /*
        提示提格式:

        ---
        知识点描述:
        xxxxx

        知识点关系:
        1. xxx -[关系]-> yyy
        2. xxx -[关系]-> yyy
        ---
         */

        KnowledgePointNode kp = knowledgePointRepository.findById(knowledgeName).orElse(null);
        if (kp == null)
            return "未查询到对应的知识点信息。";

        int num = 1;
        StringBuilder prompt = new StringBuilder();
        prompt.append("---\n");
        prompt.append("知识点描述:\n");
        prompt.append(kp.getDescription())
                .append("\n\n");
        prompt.append("知识点关系:\n");

        for (KnowledgePointRelation relatedKp : kp.getRelatedToKnowledgePoints()) {
            prompt.append(num).append(". ").append(kp.getName())
                    .append(" -[").append(relatedKp.getRelationType()).append("]-> ")
                    .append(relatedKp.getTargetNode().getName()).append("\n");
            num++;
        }
        prompt.append("---\n");

        return prompt.toString();
    }

    public void saveKnowledgePoints(KnowledgeExtractionResult result, Document sourceDocument) {

        // 未提取出任何知识点和关系
        if (result.getKnowledgePoints().isEmpty() || result.getKnowledgeRelations().isEmpty() || sourceDocument.getText() == null)
            return;

        // name, description
        // name, knowledgePoint
        Map<String, KnowledgeExtractionResult.KnowledgePointItem> knowledgePointMap = new HashMap<>();

        // <sourceName, targetName>, relationType
        Map<Pair<String, String>, String> knowledgeRelationMap = new HashMap<>();

        // 过滤&去重 节点和关系
        for (KnowledgeExtractionResult.KnowledgePointItem item : result.getKnowledgePoints()) {
            if (!knowledgePointMap.containsKey(item.getName()))
                knowledgePointMap.put(item.getName(), item);
        }

        for (KnowledgeExtractionResult.KnowledgeRelationItem item : result.getKnowledgeRelations()) {
            Pair<String, String> nodes = Pair.of(item.getSourceKnowledgePoint(), item.getTargetKnowledgePoint());
            if (!knowledgeRelationMap.containsKey(nodes)) {
                knowledgeRelationMap.put(nodes, item.getRelation());
            }
        }

        log.info("知识点数量: {}", knowledgePointMap.size());


        List<KnowledgePointNode> knowledgePoints = new ArrayList<>();
        for (String name : knowledgePointMap.keySet()) {
            KnowledgeExtractionResult.KnowledgePointItem item = knowledgePointMap.get(name);
            KnowledgePointNode kp = new KnowledgePointNode(name, item.getDescription());
            knowledgePoints.add(kp);
        }

        // 批量保存知识点
        knowledgePointRepository.saveAll(knowledgePoints);

        // 建立关系
        // 重新加载所有知识点
        Iterable<KnowledgePointNode> allPoints = knowledgePointRepository.findAllById(knowledgePointMap.keySet());
        Map<String, KnowledgePointNode> pointMap = new HashMap<>();
        allPoints.forEach(kp -> pointMap.put(kp.getName(), kp));

        // 遍历关系 Map，建立节点间关系
        for (Map.Entry<Pair<String, String>, String> entry : knowledgeRelationMap.entrySet()) {
            String sourceName = entry.getKey().getFirst();
            String targetName = entry.getKey().getSecond();
            String relationType = entry.getValue();

            KnowledgePointNode source = pointMap.get(sourceName);
            KnowledgePointNode target = pointMap.get(targetName);

            if (source == null || target == null) {
                log.warn("知识点不存在，跳过关系: {} -> {} ({})", sourceName, targetName, relationType);
                continue;
            }

            // 检查关系是否已存在
            boolean relationExists = source.getRelatedToKnowledgePoints().stream()
                    .anyMatch(r ->
                            r.getTargetNode().getName().equals(targetName)
                                    && r.getRelationType().equals(relationType));

            if (!relationExists) {
                // 创建关系实体，添加到源节点的关系列表中
                KnowledgePointRelation relation = new KnowledgePointRelation(target, relationType);
                relation.setRelationId(sourceName, targetName);
                source.getRelatedToKnowledgePoints().add(relation);
            }
        }

        // 建立关系完成的知识点节点集合
        Collection<KnowledgePointNode> knowledgePointNodes = pointMap.values();

        // 初始化 KnowledgeTextFragmentNode
        KnowledgeTextFragmentNode knowledgeTextFragmentNode = new KnowledgeTextFragmentNode(sourceDocument.getText());
        knowledgeTextFragmentNode.setKnowledgePointNodes(knowledgePointNodes.stream().toList());

        // 批量保存所有更新过的源节点并创建关系
        knowledgePointRepository.saveAll(knowledgePointNodes);
        KnowledgeTextFragmentNode savedTextFragment = knowledgeTextFragmentRepository.save(knowledgeTextFragmentNode);

        // 为父文档添加对应的文本块的元数据
        sourceDocument.getMetadata().put("fragment_id", savedTextFragment.getId());

        // 再次拆分成更细粒度的 Documents
        TextSplitter sentenceSplitter = TokenTextSplitter.builder().withChunkSize(512).build();
        List<Document> splittedDocs = sentenceSplitter.split(sourceDocument).stream().filter(
                doc -> {
                    // 过滤拆分出的空文本
                    return doc.getText() != null || doc.getText().isBlank();
                }).toList();

        log.info("文档拆分数量: {}", splittedDocs.size());

        // 向量化并存储, 可以通过documents继承的fragment_id反查KnowledgeTextFragmentNode
        vectorStore.add(splittedDocs);
    }

    /**
     * 从 LLM 响应中提取并清洗 JSON 字符串
     * 处理可能的 markdown code block 包裹及小模型输出的常见 JSON 格式问题
     */
    private String extractJson(String response) {
        if (response == null || response.isEmpty())
            return null;

        String trimmed = response.trim();

        // 去除 Markdown code block 包裹
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```");
            int contentStart = trimmed.indexOf("\n", start);
            int end = trimmed.lastIndexOf("```");
            if (contentStart > 0 && end > contentStart) {
                trimmed = trimmed.substring(contentStart + 1, end).trim();
            }
        }

        // 确保是 JSON 对象
        int jsonStart = trimmed.indexOf("{");
        int jsonEnd = trimmed.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String json = trimmed.substring(jsonStart, jsonEnd + 1);
            return cleanJson(json);
        }

        return null;
    }

    /**
     * 清洗 LLM 返回的 JSON，修复小模型常见的格式问题
     */
    private String cleanJson(String json) {
        // 修复未加引号的 key，如 {name: "foo"} → {"name": "foo"}
        json = json.replaceAll("([{,\\[])\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*:", "$1\"$2\":");

        // 去掉数组/对象末尾的多余逗号，如 ["a",] → ["a"]
        json = json.replaceAll(",\\s*([}\\]])", "$1");

        // 修复单引号值为双引号，如 'foo' → "foo"
        json = json.replaceAll("'([^']*?)'", "\"$1\"");

        // 修复缺少逗号的情况，如 {"a":"b" "c":"d"} → {"a":"b", "c":"d"}
        json = json.replaceAll("\"\\s+\"", "\",\"");

        // 修复对象之间缺少逗号，如 }{ → },{
        json = json.replaceAll("}\\s*\\{", "},{");

        // 修复数组元素之间缺少逗号，如 ][ → ],[
        json = json.replaceAll("]\\s*\\[", "],[");

        // 去除多余的空白字符（保留必要的空格）
        json = json.replaceAll("\\s+", " ").trim();

        return json;
    }


}
