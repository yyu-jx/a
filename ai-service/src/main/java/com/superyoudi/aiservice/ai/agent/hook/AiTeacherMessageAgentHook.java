package com.superyoudi.aiservice.ai.agent.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesAgentHook;
import com.alibaba.cloud.ai.graph.utils.TypeRef;
import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.common.constants.AgentConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Slf4j
@RequiredArgsConstructor
@Component
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class AiTeacherMessageAgentHook extends MessagesAgentHook {

    private final ChatPersistenceService chatPersistenceService;
    private ReactAgent agent;


    @Override
    public AgentCommand beforeAgent(List<Message> previousMessages, RunnableConfig config) {

        Long teacherId = (Long) config.metadata(AgentConstants.METADATA_USER_ID).orElseThrow();


        // 获取并拼接聊天消息
        List<Message> msgHistory = AiMessageUtils.constructChatHistory(
                () -> chatPersistenceService.getOrCreateTeacherIdChatMessages(teacherId),
                previousMessages
        );

        log.info("AI智教 历史消息列表:\n{}", msgHistory);

        // 存最新用户消息
        saveChatMessage(teacherId, previousMessages);

        return new AgentCommand(msgHistory);
    }

    @Override
    public AgentCommand afterAgent(List<Message> previousMessages, RunnableConfig config) {
        Long teacherId = (Long) config.metadata(AgentConstants.METADATA_USER_ID).orElseThrow();
        Map<String, Object> metadata = config
                .metadata(AgentConstants.METADATA_CUSTOM_METADATA, new TypeRef<Map<String, Object>>() {
                }).orElse(null);

        // 存AI回复的消息
        Long messageId = saveChatMessage(teacherId, previousMessages);

        if (messageId != null && metadata != null && metadata.containsKey(AgentConstants.CUSTOM_METADATA_CITATIONS)) {
            List<AiCitation> citations = (List<AiCitation>) metadata.getOrDefault(AgentConstants.CUSTOM_METADATA_CITATIONS, List.of());
            citations.forEach(citation ->
            {
                citation.setChatMessageId(messageId);
                chatPersistenceService.saveCitation(citation);
                log.info("AI智教 保存引用消息: 标题: {}, 数据类型: {}, 数据: {}", citation.getTitle(), citation.getDataType(), citation.getData());
            });
        }

        return super.afterAgent(previousMessages, config);
    }

    private Long saveChatMessage(Long teacherId, List<Message> messages) {
        Message lastMsg = messages.get(messages.size() - 1);

        ChatSession chatSession = chatPersistenceService.getOrCreateChatSessionByUserId(teacherId);

        if (chatSession == null) {
            log.error("chatSession为null");
            return null;
        }

        log.info("保存消息记录: 文本: {}, 角色: {}, 教师ID: {}", lastMsg.getText(), lastMsg.getMessageType(), teacherId);

        if (lastMsg.getMessageType() == MessageType.ASSISTANT) {
            return chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_AI);
        } else if (lastMsg.getMessageType() == MessageType.USER) {
            return chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_USER);
        }

        return null; // 如果消息类型不是用户或AI，返回null
    }

    @Override
    public String getName() {
        return "ai-teacher-assistant-message-agent-hook";
    }

    @Override
    public ReactAgent getAgent() {
        return agent;
    }

    @Override
    public void setAgent(ReactAgent agent) {
        this.agent = agent;
    }

    public int getOrder() {
        return 0;
    }
}
