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
                1. 知识点（Knowledge Point）：每节下设置1~5个知识点，每个知识点需包含名称(单个名词或短语)和简要描述，描述应简明扼要，突出该知识点的核心内涵或典型应用。
                2. 知识点可重复：若同一知识点在不同章节或节中有不同侧重点，则允许重复出现。
                3. 顺序编号（order）：章、节、知识点均按其在课程中的自然讲授顺序从1开始递增编号。
                4. 忽略目录中的习题、附录等与主要内容无关的条目。
                
                ### 输出格式：
                严格按照以下JSON示例的结构输出，不得包含任何额外文字、注释或Markdown标记：
                
                {
                  "courseId": 1024,
                  "courseName": "人工智能导论",
                  "chapterDataList": [
                    {
                      "chapterName": "基础概念",
                      "order": 1,
                      "sectionDataList": [
                        {
                          "sectionName": "AI定义与历史",
                          "order": 1,
                          "knowledgePointDataList": [
                            {"name": "图灵测试", "description": "判断机器是否具有智能的经典测试"},
                            {"name": "AI三大流派", "description": "符号主义、连接主义、行为主义"}
                          ]
                        },
                        {
                          "sectionName": "应用领域",
                          "order": 2,
                          "knowledgePointDataList": [
                            {"name": "计算机视觉", "description": "图像识别、目标检测等"},
                            {"name": "自然语言处理", "description": "机器翻译、文本生成等"}
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
