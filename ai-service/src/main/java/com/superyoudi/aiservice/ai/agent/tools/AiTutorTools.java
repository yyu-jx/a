package com.superyoudi.aiservice.ai.agent.tools;

import com.superyoudi.aiservice.service.AiTutorService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

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

    //    @Tool(name = "update_learning_path_stage", description = "设置当前学习路径节点的阶段")
    @SuppressWarnings("unused")
    public String updateLearningPathStage(Long pathId, String stage) {
        aiTutorService.updateLearningPathStage(pathId, stage);
        return "成功";
    }

//
//    @SuppressWarnings("unchecked")
//    @Tool(name = "test", description = "测试工具")
//    public String test(ToolContext toolContext) {
//        Map<String, Object> testData = (Map<String, Object>) toolContext.getContext().get("test_data");
//
//        if (testData != null) {
//            testData.put("test_key", "test_value");
//        }
//
//
//        return "success";
//    }

}
