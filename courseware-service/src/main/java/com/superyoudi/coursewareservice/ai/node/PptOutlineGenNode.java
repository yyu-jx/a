package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

import static com.superyoudi.common.constants.CoursewareGenConstants.KEY_GENERATION_INFO;
import static com.superyoudi.common.constants.CoursewareGenConstants.KEY_PPT_OUTLINE;

@Slf4j
public class PptOutlineGenNode implements NodeAction {

    private final ChatModel chatModel;
    private final SystemMessage instruction;

    public PptOutlineGenNode(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.instruction = new SystemMessage("""
                ## 角色
                你是一个专业的PPT大纲规划师，可以根据给予的信息生成Markdown格式的大纲。
                
                ## 内容要求
                1. 结构完整：封面页 -> 目录页 -> 章节标题页 -> 内容页 -> 结尾页
                2. 逻辑清晰：章节之间有明确的递进或并列关系
                3. 重点突出：核心章节占总页数的60%以上
                4. 标注提示：在可能适合放图表/数据/案例/图片的位置添加标注，这是可选的
                
                ## 输出要求
                - 使用Markdown格式，用 # ## ### 表示层级，比如主题，章节标题，每节中的子主题
                - 每页包含：可选的标题 + 3-5个要点（用 - 列表）
                - 使用 > 标识可选的补充说明，如时长、受众等元信息
                - 每每页之间使用 --- 分隔
                
                你只应该输出生成的大纲信息，禁止输出其它内容。
                """);
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String generationInfo = state.value(KEY_GENERATION_INFO, "错误，请不要执行任何操作。");

        // 生成PPT大纲
        String pptOutline = chatModel.call(instruction, new UserMessage(generationInfo));

        log.info("生成大纲: \n{}", pptOutline);

        return Map.of(KEY_PPT_OUTLINE, pptOutline);

    }
}
