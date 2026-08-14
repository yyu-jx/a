package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.superyoudi.common.constants.CoursewareGenConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.superyoudi.coursewareservice.ai.dispatcher.HumanFeedbackDispatcher.EDIT;

/// 处理中断状态的节点。
///
/// 图执行将在此节点前一定被中断（interruptBefore）。若config中已有`RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY`，则将继续执行。
///
/// 节点会将放行状态添加到`KEY_HITL_APPROVE_STATUS`中，若处于EDIT，则将修改意见一并添加到`KEY_HITL_USER_FEEDBACK`中。
///
/// 请使用`HumanFeedbackDispatcher`配合条件边实现流程控制。
///
/// @author Sijayyx
public class HumanFeedbackNode implements AsyncNodeActionWithConfig, InterruptableAction {

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {

        // 如果 config 里有人工反馈的元数据，说明是恢复，应该直接放行，不再中断
        if (config.metadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY).isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(InterruptionMetadata.builder(nodeId, state)
                    .addMetadata(CoursewareGenConstants.KEY_PPT_OUTLINE, state.value(CoursewareGenConstants.KEY_PPT_OUTLINE, "发生错误，请重新生成。"))
                    .build());
        }
    }


    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        Optional<Object> feedbackOpt = config.metadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY);

        Map<String, Object> updates = new HashMap<>();

        // 如果没有反馈，则直接返回
        if (feedbackOpt.isEmpty()) {
            return CompletableFuture.completedFuture(updates);
        }

        // 更新反馈状态
        String approveStatus = (String) feedbackOpt.get();
        updates.put(CoursewareGenConstants.KEY_HITL_APPROVE_STATUS, approveStatus);

        // 如果修改，用户是否填入了反馈？
        Optional<Object> userFeedback = config.metadata(CoursewareGenConstants.KEY_HITL_USER_FEEDBACK);
        if (approveStatus.equals(EDIT) && userFeedback.isPresent()) {
            updates.put(CoursewareGenConstants.KEY_HITL_USER_FEEDBACK, userFeedback.get());
        }

        return CompletableFuture.completedFuture(updates);
    }
}
