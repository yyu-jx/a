package com.superyoudi.aiservice.ai.agent;

import com.superyoudi.aiservice.utils.AiMessageUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * @author Sijayyx
 */
@Component
public class CourseDataGenerateModel {

    private final ChatModel chatModel;

    public CourseDataGenerateModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String call(Long courseId, String courseName, String courseDesc) {
        String prompt = String.format("""
                你是一名资深课程规划师，擅长根据课程信息生成结构清晰、逻辑递进的章、节和知识点体系。请根据以下课程信息，生成完整的课程大纲结构。
                
                **课程ID**: %d
                **课程名称**：%s
                **课程描述**：%s
                
                ### 生成要求：
                1. 知识点可重复：若同一知识点在不同章节或节中有不同侧重点，则允许重复出现。
                2. 顺序编号（order）：章、节、知识点均按其在课程中的自然讲授顺序从1开始递增编号。
                3. 忽略目录中的习题、附录等与主要内容无关的条目。
                
                # 知识点抽取规则
                1. **知识点粒度**：抽取原子性的、独立可理解的知识单元。避免抽取过于宽泛（如“数学”）或过于琐碎（如“这是一个例子”）的短语。
                2. **知识点命名**：使用**标准术语**，长度控制在2-10个汉字或英文单词。
                3. **知识点描述**：用简洁且准确的话描述该知识点的核心含义，相当于一个简短的百科解释。
                4. 知识点必须与一个已有知识点建立关系。
                
                # 关系类型定义 relation（严格遵循以下规范）
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
                
                
                ### 输出格式：
                严格按照以下JSON示例的结构输出，不得包含任何额外文字、注释或Markdown标记：
                
                {
                  "courseId": 1024,
                  "courseName": "数据结构与算法",
                  "chapterDataList": [
                    {
                      "chapterName": "线性结构",
                      "order": 1,
                      "sectionDataList": [
                        {
                          "sectionName": "数组与链表",
                          "order": 1,
                          "extractionResults": [
                            {
                              "knowledgePoints": [
                                {"name": "数组", "description": "连续内存存储，随机访问O(1)，插入删除O(n)"},
                                {"name": "链表", "description": "离散内存存储，插入删除O(1)，随机访问O(n)"},
                                {"name": "索引访问", "description": "通过下标直接访问元素的能力"}
                              ],
                              "knowledgeRelations": [
                                {"relation": "DIFFERENTIATES_FROM", "sourceKnowledgePoint": "数组", "targetKnowledgePoint": "链表"},
                                {"relation": "HAS_A", "sourceKnowledgePoint": "数组", "targetKnowledgePoint": "索引访问"},
                                {"relation": "SIMILAR_TO", "sourceKnowledgePoint": "链表", "targetKnowledgePoint": "栈"}
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                
                请根据上述要求，为给定的课程名称和目录生成完整的大纲JSON，不要输出其它内容。
                """, courseId, courseName, courseDesc);

        return AiMessageUtils.extractJson(chatModel.call(prompt));
    }


}
