package com.superyoudi.coursewareservice.ai.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.superyoudi.common.constants.CoursewareGenConstants.KEY_HITL_APPROVE_STATUS;


@Slf4j
public class HumanFeedbackDispatcher implements EdgeAction {

    public static final String APPROVE = "APPROVE";
    public static final String EDIT = "EDIT";
    public static final String REJECT = "REJECT";

    /// 批准的目标节点
    private final String approveTarget;
    /// 修正的目标节点
    private final String editTarget;
    /// 拒绝的目标节点
    private final String rejectTarget;

    public HumanFeedbackDispatcher(String approveTarget, String editTarget, String rejectTarget) {
        this.approveTarget = approveTarget;
        this.editTarget = editTarget;
        this.rejectTarget = rejectTarget;
    }

    @Override
    public String apply(OverAllState state) throws Exception {

        // 在这里可以控制用户反馈的状态
        String feedback = state.value(KEY_HITL_APPROVE_STATUS, "ERROR");

        // APPROVE / EDIT / REJECT
        return switch (feedback) {
            case APPROVE -> approveTarget;
            case EDIT -> editTarget;
            case REJECT -> rejectTarget;
            default -> {
                log.error("无效的反馈: {}", feedback);
                yield rejectTarget;
            }
        };
    }
}
