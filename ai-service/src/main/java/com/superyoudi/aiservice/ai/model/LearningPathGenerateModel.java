package com.superyoudi.aiservice.ai.model;

import com.superyoudi.aiservice.utils.AiMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * @author Sijayyx
 */
@RequiredArgsConstructor
@Component
public class LearningPathGenerateModel {

    private final ChatModel chatModel;

    public String call(Long studentId, Long courseId, String kpMasteryInfo) {
        String prompt = String.format("""
                你是一个学习路径规划师，擅长根据学生掌握情况和知识点列表，将知识点聚合为节点，再整理为有序的学习路径。
                
                ## 规则：
                一个学习路径节点包含1-5个知识点。符合以下任一条件的知识点应聚合到同一节点：
                - 共享同一个父知识点的知识点
                - 存在强逻辑关联的知识点（如前置-后继关系、因果推导关系）
                - 内容琐碎但主题高度相关的知识点
                
                ## 数量要求
                - 尽量生成恰好5个节点
                - 若知识点数量不足以支撑5个节点，则生成可生成的最多节点数
                - 无法被聚合到任何节点的零散知识点应直接丢弃，不纳入输出
                
                ## 约束条件：
                - 保持给定的知识点顺序不变，不得调整顺序
                - 节点命名应简洁概括节点内知识点的共同主题
                
                ## 所需信息：
                - 学生ID: %d
                - 课程ID: %d
                - 知识点与学生掌握情况：
                %s
                
                ## 输出JSON示例：
                {
                  "studentId": 12345,
                  "courseId": 67890,
                  "additionalInfo": "简要说明该路径的聚焦方向或针对学生薄弱点的学习建议",
                  "pathNodeDataList": [
                    {
                      "nodeName": "函数与极限",
                      "knowledgeIds": ["K001", "K002"]
                    },
                    {
                      "nodeName": "导数入门",
                      "knowledgeIds": ["K003"]
                    }
                  ]
                }
                
                只输出最终的JSON文本，不要输出其它内容。
                """, studentId, courseId, kpMasteryInfo);

        return AiMessageUtils.extractJson(chatModel.call(prompt));
    }


}
