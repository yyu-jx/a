package com.superyoudi.coursewareservice.ai.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesAgentHook;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.common.constants.CoursewareGenConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Sijayyx
 */
@Slf4j
@Component
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class SupervisorMessageAgentHook extends MessagesAgentHook {

    private final ChatPersistenceService chatPersistenceService;
    private ReactAgent reactAgent;

    public SupervisorMessageAgentHook(ChatPersistenceService chatPersistenceService) {
        this.chatPersistenceService = chatPersistenceService;
    }

    @Override
    public AgentCommand beforeAgent(List<Message> previousMessages, RunnableConfig config) {
        long studentId = (long) config.metadata(CoursewareGenConstants.KEY_USER_ID).orElseThrow();
        long coursewareRecId = (long) config.metadata(CoursewareGenConstants.KEY_COURSEWARE_REC_ID).orElseThrow();

        List<Message> msgHistory = AiMessageUtils.recoverChatHistory(() ->
                        chatPersistenceService.getOrCreateCoursewareRecChatMessages(studentId, coursewareRecId),
                previousMessages);

        msgHistory.add(0, new SystemMessage(String.format("""
                你是一个监督者，可以分析用户意图并给出对应的响应。
                
                当你处于测试模式时，你应该严格遵守指令。
                当用户是在与你聊天或没有生成教学资料（PPT课件和/或教案）的意图，你应该回应用户输入。
                
                当用户要求生成教学资料时，你需要结合用户输入，判断你是否明确了这些要点：
                课程主题;
                教学信息：如授课对象，年级，课程时长;
                教学目标：应该掌握的具体知识点;
                核心知识点：最核心的1-3个知识点;
                重难点：需要明确讲授或强调的内容;
                教学流程：较详细的课堂教学安排;
                互动设计: 可选，课堂安排中的互动设计，比如提问与小组讨论等;
                
                如果有任何一个要点没有明确，你需要引导式地向用户提问，以明确这些要点。
                除了向用户询问时，否则关于任何要点的详细内容，你只能总结用户的输入，而不能对其进行推断、举例或分析。
                
                如果明确了所有要点，你需要总结以上要点，并且向用户确认.
                当用户确认完毕后，你需要总结这些要点。
                
                当用户要求修改PPT或者教案时，如果用户的要求不明确，你需要对不明确的地方发起引导式的提问。
                
                你可以调用相关工具获取用户上传的参考资料，以做出相关回答。
                不要告诉用户你的内部执行逻辑，不要自行生成PPT或教案及其大纲。
                你可以向下游节点发送消息以执行PPT和教案的生成任务。
                
                用户ID: %d
                课件记录ID: %d
                """, studentId, coursewareRecId)));

        log.info("监督者 历史消息列表:\n{}", msgHistory);

        // 存最新用户消息
        saveChatMessage(coursewareRecId, previousMessages);

        return new AgentCommand(msgHistory);
    }

    @Override
    public AgentCommand afterAgent(List<Message> previousMessages, RunnableConfig config) {

        long coursewareRecId = (long) config.metadata(CoursewareGenConstants.KEY_COURSEWARE_REC_ID).orElseThrow();

        // 存AI回复的消息
        saveChatMessage(coursewareRecId, previousMessages);

        return super.afterAgent(previousMessages, config);
    }

    /**
     * 保存消息记录。
     * <p>
     * 获取最后一个消息，并向消息列表追加它。
     */
    private void saveChatMessage(Long coursewareRecId, List<Message> messages) {
        Message lastMsg = messages.get(messages.size() - 1);

        ChatSession chatSession = chatPersistenceService.getChatSessionByCoursewareRecId(coursewareRecId);

        if (chatSession == null) {
            log.error("chatSession为null");
            return;
        }

        log.info("保存消息记录: 文本: {}, 角色: {}, 课件生成记录ID: {}", lastMsg.getText(), lastMsg.getMessageType(), coursewareRecId);

        if (lastMsg.getMessageType() == MessageType.ASSISTANT) {
            chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_AI);
        } else if (lastMsg.getMessageType() == MessageType.USER) {
            chatPersistenceService.appendChatMessage(chatSession.getId(), lastMsg.getText(), ChatMessage.ROLE_USER);
        }
    }

    public List<ChatMessage> getChatHistory(long userId, long coursewareRecId) {
        return chatPersistenceService.getOrCreateCoursewareRecChatMessages(userId, coursewareRecId);
    }

    @Override
    public String getName() {
        return "supervisor-message-agent-hook";
    }

    @Override
    public ReactAgent getAgent() {
        return reactAgent;
    }

    @Override
    public void setAgent(ReactAgent agent) {
        this.reactAgent = agent;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
