package com.superyoudi.kdbservice.service;

import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.superyoudi.aiservice.ai.model.KnowledgeGraphExtractionModel;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.graph.KnowledgePointRelation;
import com.superyoudi.kdbservice.graph.KnowledgeTextFragmentNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import com.superyoudi.kdbservice.repository.KnowledgeTextFragmentRepository;
import lombok.extern.slf4j.Slf4j;
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


    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgeTextFragmentRepository knowledgeTextFragmentRepository;
    private final VectorStore vectorStore;
    private final KnowledgeGraphExtractionModel knowledgeGraphExtractionModel;

    public KnowledgeGraphService(KnowledgePointRepository knowledgePointRepository,
                                 KnowledgeTextFragmentRepository knowledgeTextFragmentRepository,
                                 VectorStore vectorStore,
                                 KnowledgeGraphExtractionModel knowledgeGraphExtractionModel) {
        this.knowledgeGraphExtractionModel = knowledgeGraphExtractionModel;
        this.knowledgePointRepository = knowledgePointRepository;
        this.knowledgeTextFragmentRepository = knowledgeTextFragmentRepository;
        this.vectorStore = vectorStore;

        log.info("KnowledgeGraphService 初始化。");
    }

    /**
     * 抽取原始文本的知识点并构建知识图谱。原始文本会被存入向量数据库中。
     *
     */
    public void extractKnowledgePoints(String originalText) {
        Document document = new Document(originalText);

        SentenceSplitter sentenceSplitter = new SentenceSplitter();

        // 拆分原始文本块，避免超出token限制
        List<Document> documents = sentenceSplitter.split(document);

        for (Document doc : documents) {
            if (doc.getText() == null || doc.getText().isBlank())
                continue;

            String text = doc.getText();

            String contentJson = knowledgeGraphExtractionModel.call(text);

            log.info("抽取结果:\n{}", contentJson);

            try {
                KnowledgeExtractionResult result = AiMessageUtils.convertJsonValue(contentJson, KnowledgeExtractionResult.class);
                if (result != null)
                    saveKnowledgePoints(result, doc);
            } catch (Exception e) {
                log.error("解析失败", e);
            }
        }
    }

    /**
     * 此方法保存知识图谱中的知识点和关系，并对原始文本进行ETL。
     * <p>
     * 传入的原始文本会被进行一次粗粒度的分割以确保不会超出AI的上下文长度限制。每个分割出的Document会由AI抽取知识点。
     * <p>
     * 抽取知识点后，JSON数据被解析为KnowledgeExtractionResult并构建知识点及其关系。它和附加的原始文本被一并传入此方法。
     * <p>
     * 随后，KnowledgeExtractionResult中的知识点及关系被解析成Map并去重。关系引用的知识点必须存在。知识点会先批量存入Neo4j以构建ID，随后它再次被检索出来，用来构建知识点间关系。
     * <p>
     * 知识点的关系和知识点本身同样会被去重。知识点关系由KnowledgePointRelation表示，它由源知识点指向目标，包含关系类型和目标知识点。
     * <p>
     * 关系会暂时存入源知识点的关系列表中。随后知识点列表被用来构建KnowledgeTextFragmentNode。它包含了原始文本及其抽取出的知识点关系。
     * <p>
     * 在KnowledgeTextFragmentNode存放完毕后，sourceDocument会被拆分成更细粒度的Documents。其指向的KnowledgeTextFragmentNode的ID会被添加进元数据中。
     * <p>
     * 最后，KnowledgeTextFragmentNode和细粒度的Documents会被存入数据库。在执行检索增强生成时，Documents会首先检索出来，随后可根据KnowledgeTextFragmentNode的ID反查出知识点关系网。
     *
     * @param result         知识点抽取结果
     * @param sourceDocument 原始文本片段
     */
    public void saveKnowledgePoints(KnowledgeExtractionResult result, Document sourceDocument) {

        if (sourceDocument == null)
            return;

        List<KnowledgePointNode> knowledgePointNodes = saveKnowledgePoints(result);

        // 初始化 KnowledgeTextFragmentNode
        KnowledgeTextFragmentNode knowledgeTextFragmentNode = new KnowledgeTextFragmentNode(sourceDocument.getText());
        knowledgeTextFragmentNode.setKnowledgePointNodes(knowledgePointNodes.stream().toList());

        // 批量保存所有更新过的源节点并创建关系
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
        try {
            vectorStore.add(splittedDocs);
        } catch (Exception e) {
            log.error("向量数据存储失败", e);
        }
    }

    public List<KnowledgePointNode> saveKnowledgePoints(KnowledgeExtractionResult result) {

        // 未提取出任何知识点和关系
        if (result.getKnowledgePoints().isEmpty() || result.getKnowledgeRelations().isEmpty())
            return null;

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
        // 重新加载所有知识点-
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
        // 批量保存所有更新过的源节点并创建关系
        knowledgePointRepository.saveAll(knowledgePointNodes);

        return knowledgePointNodes.stream().toList();
    }

    /// 根据查询文本的语义获取相关知识点关系和资料
    ///
    /// 输出：
    /// ```markdown
    /// ---
    /// 知识点及关系:
    /// 1. XXX -[关系]-> YYY
    /// 2. XXX -[关系]-> YYY
    /// 3. XXX -[关系]-> YYY
    ///
    /// 相关资料:
    /// 1. xxxxxxxxx
    /// 2. xxxxxxxxx
    /// ---
    /// ```
    ///
    /// @param queryText 查询文本
    /// @return 提示词
    public String retrieveKnowledgeGraph(String queryText) {

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
                    .ifPresent(fragment ->
                            knowledgePointNodes.addAll(fragment.getKnowledgePointNodes()));
        });

        /*
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

    /// 查询知识点注释并列出与其相关的知识点
    ///
    /// 输出:
    /// ```markdown
    /// ---
    /// 知识点描述:
    /// xxxxx
    ///
    /// 知识点关系:
    /// 1. xxx -[关系]-> yyy
    /// 2. xxx -[关系]-> yyy
    /// ---
    /// ```
    ///
    /// @param knowledgeName 知识点名称
    /// @return 提示词
    public String retrieveKnowledgePoint(String knowledgeName) {

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

    /**
     * 根据查询文本的语义检索文档
     *
     * @param query
     * @return
     */
    public String retrieveDocuments(String query) {
        // 查询知识点
        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query)
                .similarityThreshold(0.7)
                .topK(3)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        StringBuilder sb = new StringBuilder();
        sb.append("---\n");

        int i = 1;
        for (Document doc : documents) {
            sb.append(i).append(". ").append(doc.getText()).append("\n");
        }

        sb.append("---\n");

        return sb.toString();
    }


}
