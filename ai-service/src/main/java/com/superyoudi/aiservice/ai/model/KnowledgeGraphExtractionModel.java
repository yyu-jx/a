package com.superyoudi.aiservice.ai.model;

import com.superyoudi.aiservice.utils.AiMessageUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 抽取知识图谱
 *
 * @author Sijayyx
 */
@Component
public class KnowledgeGraphExtractionModel {

    private final ChatClient chatClient;

    public KnowledgeGraphExtractionModel(ChatClient.Builder chatClient) {
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
    }

    public String call(String text) {
        String contentJson = chatClient.prompt(
                        String.format("""
                                请抽取如下文本的知识点与关系:
                                %s
                                """, text))
                .call()
                .content();

        // 清洗Json
        return AiMessageUtils.extractJson(contentJson);

    }
}
