package com.superyoudi.aiservice.ai.agent.tools;

import com.superyoudi.aiservice.service.AiTutorService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Component
public class AiTutorTools {

    private final AiTutorService aiTutorService;


    public AiTutorTools(AiTutorService aiTutorService) {
        this.aiTutorService = aiTutorService;
    }

    @Tool(name = "update_learning_path_stage", description = "设置当前学习路径节点的阶段")
    @SuppressWarnings("unused")
    public String updateLearningPathStage(Long pathId, String stage) {
        aiTutorService.updateLearningPathStage(pathId, stage);
        return "成功";
    }

    public String composePathNodeHomework(long pathNodeId,
                                          List<String> knowledgePoints,
                                          Map<String, Integer> questionConfig,
                                          int totalScore) {

        aiTutorService.composePathNodeHomework(pathNodeId, knowledgePoints, questionConfig, totalScore);

    }
}
