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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 消息Agent Hook抽象基类
 *
 * @author Sijayyx
 */
@Slf4j
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public abstract class AbstractMessageAgentHook extends MessagesAgentHook {

    protected final ChatPersistenceService chatPersistenceService;
    private ReactAgent agent;

    protected AbstractMessageAgentHook(ChatPersistenceService chatPersistenceService) {
        this.chatPersistenceService = chatPersistenceService;
    }

    @Override
    public AgentCommand beforeAgent(List<Message> previousMessages, RunnableConfig config) {

        List<Message> historyMessages = getHistoryMessages(previousMessages, config);
        log.info("{} 历史消息列表:\n{}", getLogTag(), historyMessages);

        // 存最新用户消息
        saveChatMessage(previousMessages, config);

        return new AgentCommand(historyMessages);
    }

    @Override
    public AgentCommand afterAgent(List<Message> previousMessages, RunnableConfig config) {
        Map<String, Object> metadata = config
                .metadata(AgentConstants.METADATA_CUSTOM_METADATA, new TypeRef<Map<String, Object>>() {
                }).orElse(null);

        // 存AI回复的消息
        Long messageId = saveChatMessage(previousMessages, config);

        if (messageId != null && metadata != null && metadata.containsKey(AgentConstants.CUSTOM_METADATA_CITATIONS)) {
            saveCitations(messageId, metadata);
        }

        return super.afterAgent(previousMessages, config);
    }

    /**
     * 保存消息记录
     */
    protected Long saveChatMessage(List<Message> messages, RunnableConfig config) {
        Message lastMsg = messages.get(messages.size() - 1);

        ChatSession chatSession = getOrCreateChatSession(config);

        if (chatSession == null) {
            log.error("{} chatSession为null", getLogTag());
            return null;
        }

        log.info("{} 保存消息记录: 文本: {}, 角色: {}",
                getLogTag(), lastMsg.getText(), lastMsg.getMessageType());

        if (lastMsg.getMessageType() == MessageType.ASSISTANT) {
            return chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_AI);
        } else if (lastMsg.getMessageType() == MessageType.USER) {
            return chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_USER);
        }

        return null;
    }

    /**
     * 保存引用消息
     */
    @SuppressWarnings("unchecked")
    private void saveCitations(Long messageId, Map<String, Object> metadata) {
        var citations = (List<AiCitation>) metadata.getOrDefault(AgentConstants.CUSTOM_METADATA_CITATIONS, List.of());

        citations.forEach(citation -> {
            citation.setChatMessageId(messageId);
            chatPersistenceService.saveCitation(citation);
            log.info("{} 保存引用消息: 标题: {}, 数据类型: {}, 数据: {}",
                    getLogTag(), citation.getTitle(), citation.getDataType(), citation.getData());
        });
    }


    /**
     * 获取历史消息
     */
    protected List<Message> getHistoryMessages(List<Message> previousMessages, RunnableConfig config) {
        return AiMessageUtils.constructChatHistory(() -> getChatMessagesSupplier(config).get(), previousMessages);
    }

    /**
     * 获取或创建聊天会话
     */
    protected abstract ChatSession getOrCreateChatSession(RunnableConfig config);

    /**
     * 获取聊天消息
     */
    protected abstract Supplier<List<ChatMessage>> getChatMessagesSupplier(RunnableConfig config);


    protected abstract String getLogTag();

    @Override
    public ReactAgent getAgent() {
        return agent;
    }

    @Override
    public void setAgent(ReactAgent agent) {
        this.agent = agent;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}