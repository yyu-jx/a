package com.superyoudi.aiservice.utils;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Sijayyx
 */
@Slf4j
public class AiMessageUtils {
    /**
     * 宽松的 JSON 解析器，容忍小模型输出的格式问题
     */
    public static final ObjectMapper LENIENT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)  // 允许无引号字段名
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)         // 允许单引号
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)        // 允许尾随逗号
            .build()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String EMPTY_STRING = "";

    public static List<Message> recoverChatHistory(Supplier<List<ChatMessage>> chatHistorySupplier, List<Message> previousMessages) {
        List<ChatMessage> chatMessages = chatHistorySupplier.get();

        if (chatMessages == null) {
            return previousMessages;
        }

        ArrayList<Message> msgHistory = chatMessages.stream()
                // 推断类型
                .<Message>map(message -> {
                    if (message.getRole().equals(ChatMessage.ROLE_USER)) {
                        return new UserMessage(message.getMessage());
                    } else if (message.getRole().equals(ChatMessage.ROLE_AI)) {
                        return new AssistantMessage(message.getMessage());
                    } else return null;
                }).filter(Objects::nonNull) // 过滤空消息
                .collect(Collectors.toCollection(ArrayList::new));

        if (previousMessages != null)
            msgHistory.addAll(previousMessages);

        return msgHistory;
    }

    /**
     * 格式化聊天消息列表，只包含AI和用户的对话
     *
     * @param messages 聊天消息列表
     * @return 格式化后的对话文本
     */
    public static String formatChatHistory(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        return messages.stream()
                .filter(msg -> msg != null && msg.getMessage() != null)
                .filter(msg -> ChatMessage.ROLE_AI.equals(msg.getRole())
                        || ChatMessage.ROLE_USER.equals(msg.getRole()))
                .map(msg -> msg.getRole() + ": " + msg.getMessage())
                .collect(Collectors.joining("\n"));
    }

    /// 处理AI的流式输出，将NodeOutput的文本解析出来。
    ///
    /// 用法:
    ///
    /// ```java
    /// Flux<String> message = Flux<NodeOutput>.map(nodeOutput ->
    ///         AiMessageUtils.handleStreamingOutput(nodeOutput, interruptionCallback, streamingFinishedCallback));
    /// ```
    ///
    /// @param output                    Agent/Graph的节点输出。
    /// @param interruptionCallback      图执行中断的回调。
    /// @param streamingFinishedCallback 流式输出结束的回调，可获取输出的全部文本。
    /// @param dataInFinish              流式输出结束时的额外数据，附在`\[DONE:%s\]`中。若无数据，则返回`\[DONE\]`。
    /// @return 输出文本。
    public static String handleStreamingOutput(@NotNull NodeOutput output,
                                               @Nullable Function<InterruptionMetadata, String> interruptionCallback,
                                               @Nullable Consumer<String> streamingFinishedCallback,
                                               @Nullable Supplier<String> dataInFinish) {

        // 处理HITL中断
        if (output instanceof InterruptionMetadata interruptionMetadata) {
            if (interruptionCallback != null)
                return interruptionCallback.apply(interruptionMetadata);
        }
        // 处理流式输出
        else if (output instanceof StreamingOutput<?> streamingOutput) {
            OutputType outputType = streamingOutput.getOutputType();
            Message message = streamingOutput.message();

            if (message == null)
                return EMPTY_STRING;

            // 判断节点输出类型
            if (outputType == OutputType.GRAPH_NODE_STREAMING || outputType == OutputType.AGENT_MODEL_STREAMING) {
                // 流式输出
                String chunk = message.getText();
                if (chunk != null)
                    return chunk;
            } else if (outputType == OutputType.GRAPH_NODE_FINISHED || outputType == OutputType.AGENT_MODEL_FINISHED) {
                // 输出结束
                if (streamingFinishedCallback != null) {
                    String allMessage = message.getText(); // 全部输出
                    streamingFinishedCallback.accept(allMessage);
                }

                if (dataInFinish != null)
                    // 输出结束并附带额外数据
                    return String.format("[DONE:%s]", dataInFinish.get());

                return "[DONE]";
            }
        }
        return EMPTY_STRING;
    }

    public static <T> T readJsonValue(String json, Class<T> type) {
        try {
            return LENIENT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("JSON 解析失败: {}", json, e);
            return null;
        }

    }

    /**
     * 从 LLM 响应中提取并清洗 JSON 字符串
     * 处理可能的 markdown code block 包裹及小模型输出的常见 JSON 格式问题
     */
    public static String extractJson(String response) {
        if (response == null || response.isEmpty())
            return null;

        String trimmed = response.trim();

        // 去除 Markdown code block 包裹
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```");
            int contentStart = trimmed.indexOf("\n", start);
            int end = trimmed.lastIndexOf("```");
            if (contentStart > 0 && end > contentStart) {
                trimmed = trimmed.substring(contentStart + 1, end).trim();
            }
        }

        // 确保是 JSON 对象
        int jsonStart = trimmed.indexOf("{");
        int jsonEnd = trimmed.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String json = trimmed.substring(jsonStart, jsonEnd + 1);
            return cleanJson(json);
        }

        return null;
    }

    /**
     * 清洗 LLM 返回的 JSON，修复小模型常见的格式问题
     */
    private static String cleanJson(String json) {
        // 修复未加引号的 key，如 {name: "foo"} → {"name": "foo"}
        json = json.replaceAll("([{,\\[])\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*:", "$1\"$2\":");

        // 去掉数组/对象末尾的多余逗号，如 ["a",] → ["a"]
        json = json.replaceAll(",\\s*([}\\]])", "$1");

        // 修复单引号值为双引号，如 'foo' → "foo"
        json = json.replaceAll("'([^']*?)'", "\"$1\"");

        // 修复缺少逗号的情况，如 {"a":"b" "c":"d"} → {"a":"b", "c":"d"}
        json = json.replaceAll("\"\\s+\"", "\",\"");

        // 修复对象之间缺少逗号，如 }{ → },{
        json = json.replaceAll("}\\s*\\{", "},{");

        // 修复数组元素之间缺少逗号，如 ][ → ],[
        json = json.replaceAll("]\\s*\\[", "],[");

        // 去除多余的空白字符（保留必要的空格）
        json = json.replaceAll("\\s+", " ").trim();

        return json;
    }


}
