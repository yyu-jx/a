package com.superyoudi.aiservice.ai.agent.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.common.constants.AgentConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 *
 *
 *
 * @author Sijayyx
 */
@Slf4j
@Component
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class AiTutorMessageAgentHook extends AbstractMessageAgentHook {


    public AiTutorMessageAgentHook(ChatPersistenceService chatPersistenceService) {
        super(chatPersistenceService);
    }

    @Override
    protected ChatSession getOrCreateChatSession(RunnableConfig config) {
        Long courseId = (Long) config.metadata(AgentConstants.METADATA_COURSE_ID).orElseThrow();
        Long studentId = (Long) config.metadata(AgentConstants.METADATA_USER_ID).orElseThrow();
        return chatPersistenceService.getOrCreateChatSessionByCourseRel(studentId, courseId);
    }

    @Override
    protected Supplier<List<ChatMessage>> getChatMessagesSupplier(RunnableConfig config) {
        Long courseId = (Long) config.metadata(AgentConstants.METADATA_COURSE_ID).orElseThrow();
        Long studentId = (Long) config.metadata(AgentConstants.METADATA_USER_ID).orElseThrow();
        return () -> chatPersistenceService.getOrCreateCourseRelChatMessages(studentId, courseId);
    }

    @Override
    protected String getLogTag() {
        return "AI导师";
    }

    @Override
    public String getName() {
        return "ai-tutor-message-agent-hook";
    }
}
