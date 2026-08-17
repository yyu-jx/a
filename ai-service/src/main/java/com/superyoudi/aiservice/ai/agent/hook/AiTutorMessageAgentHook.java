package com.superyoudi.aiservice.ai.agent.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesAgentHook;
import com.alibaba.cloud.ai.graph.utils.TypeRef;
import com.superyoudi.aiservice.ai.agent.AiTutorAgent;
import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
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
public class AiTutorMessageAgentHook extends MessagesAgentHook {
    private final ChatPersistenceService chatPersistenceService;
    private ReactAgent agent;

    @Override
    public AgentCommand beforeAgent(List<Message> previousMessages, RunnableConfig config) {

        // 这里能拿到用户发来的消息，在其之前恢复消息历史
        Long courseId = (Long) config.metadata(AiTutorAgent.METADATA_COURSE_ID).orElseThrow();
        Long studentId = (Long) config.metadata(AiTutorAgent.METADATA_STUDENT_ID).orElseThrow();

        List<Message> msgHistory = AiMessageUtils.recoverChatHistory(() ->
                        chatPersistenceService.getOrCreateCourseRelChatMessages(studentId, courseId),
                previousMessages);

        log.info("AI导师 历史消息列表:\n{}", msgHistory);

        // 存最新用户消息
        saveChatMessage(studentId, courseId, previousMessages);

        return new AgentCommand(msgHistory);
    }

    @Override
    public AgentCommand afterAgent(List<Message> previousMessages, RunnableConfig config) {
        // 这里能拿到用户消息、工具调用消息、AI回复的消息
        Long courseId = (Long) config.metadata(AiTutorAgent.METADATA_COURSE_ID).orElseThrow();
        Long studentId = (Long) config.metadata(AiTutorAgent.METADATA_STUDENT_ID).orElseThrow();
        Map<String, Object> metadata = config
                .metadata(AiTutorAgent.METADATA_CUSTOM_METADATA, new TypeRef<Map<String, Object>>() {
                }).orElse(null);


        // 存AI回复的消息
        Long messageId = saveChatMessage(studentId, courseId, previousMessages);

        if (metadata != null && metadata.containsKey("citations")) {
            List<AiCitation> citations = (List<AiCitation>) metadata.get("citations");
            citations.forEach(citation ->
            {
                citation.setChatMessageId(messageId);
                chatPersistenceService.saveCitation(citation);
                log.info("AI导师 保存引用消息: 标题: {}, 数据类型: {}, 数据: {}", citation.getTitle(), citation.getDataType(), citation.getData());
            });
        }

        // 存引用消息: metadata.citations

        return super.afterAgent(previousMessages, config);
    }

    /**
     * 保存消息记录。
     * <p>
     * 获取最后一个消息，并向消息列表追加它。
     */
    private Long saveChatMessage(Long studentId, Long courseId, List<Message> messages) {
        Message lastMsg = messages.get(messages.size() - 1);

        log.info("保存消息记录: 文本: {}, 角色: {}, 学生ID/课程ID: {}/{}", lastMsg.getText(), lastMsg.getMessageType(), studentId, courseId);

        if (lastMsg.getMessageType() == MessageType.ASSISTANT) {
            return chatPersistenceService.appendChatMessage(studentId, courseId, lastMsg.getText(), ChatMessage.ROLE_AI);
        } else if (lastMsg.getMessageType() == MessageType.USER) {
            return chatPersistenceService.appendChatMessage(studentId, courseId, lastMsg.getText(), ChatMessage.ROLE_USER);
        }

        return null; // 如果消息类型不是用户或AI，返回null
    }

    @Override
    public String getName() {
        return "ai-tutor-message-model-hook";
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
