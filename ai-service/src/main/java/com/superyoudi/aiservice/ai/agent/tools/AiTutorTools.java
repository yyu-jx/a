package com.superyoudi.aiservice.ai.agent.tools;

import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.common.constants.AgentConstants;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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

    @SuppressWarnings("unused")
    @Tool(name = "compose_path_node_homework", description = "生成学习路径节点的作业")
    public String composePathNodeHomework(@ToolParam(description = "当前学习路径节点ID") long pathNodeId,
                                          @ToolParam(description = "作业标题") String title,
                                          @ToolParam(description = "题目有关的知识点列表") List<String> knowledgePoints,
                                          @ToolParam(description = "题目配置，key为题目类型，value为题目数量") Map<String, Integer> questionConfig,
                                          @ToolParam(description = "作业总分") int totalScore,
                                          ToolContext context) {

        Long courseId = (Long) context.getContext().get(AgentConstants.METADATA_COURSE_ID);
        Long studentId = (Long) context.getContext().get(AgentConstants.METADATA_USER_ID);

        // 如果已经生成了作业，就不生成了
        if (aiTutorService.checkPathNodeHomeWorkExists(pathNodeId, courseId, studentId)) {
            return "作业已经生成";
        }

        aiTutorService.composePathNodeHomework(pathNodeId, courseId, studentId, knowledgePoints, title, questionConfig, totalScore);

        return "成功";
    }
}
