package com.superyoudi.coursewareservice.ai.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.superyoudi.aiservice.ai.agent.hook.AbstractMessageAgentHook;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.common.constants.CoursewareGenConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Sijayyx
 */
@Slf4j
@Component
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class SupervisorMessageAgentHook extends AbstractMessageAgentHook {

    protected SupervisorMessageAgentHook(ChatPersistenceService chatPersistenceService) {
        super(chatPersistenceService);
    }

    public List<ChatMessage> getChatHistory(long userId, long coursewareRecId) {
        return chatPersistenceService.getOrCreateCoursewareRecChatMessages(userId, coursewareRecId);
    }

    @Override
    protected ChatSession getOrCreateChatSession(RunnableConfig config) {
        long coursewareRecId = (long) config.metadata(CoursewareGenConstants.KEY_COURSEWARE_REC_ID).orElseThrow();

        return chatPersistenceService.getChatSessionByCoursewareRecId(coursewareRecId);
    }

    @Override
    protected Supplier<List<ChatMessage>> getChatMessagesSupplier(RunnableConfig config) {
        long studentId = (long) config.metadata(CoursewareGenConstants.KEY_USER_ID).orElseThrow();
        long coursewareRecId = (long) config.metadata(CoursewareGenConstants.KEY_COURSEWARE_REC_ID).orElseThrow();
        return () -> chatPersistenceService.getOrCreateCoursewareRecChatMessages(studentId, coursewareRecId);
    }

    @Override
    protected String getLogTag() {
        return "SupervisorNode";
    }

    @Override
    public String getName() {
        return "supervisor-message-agent-hook";
    }
}


