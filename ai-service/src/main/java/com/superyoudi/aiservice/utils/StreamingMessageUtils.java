package com.superyoudi.aiservice.utils;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Sijayyx
 */
public class StreamingMessageUtils {

    /// 处理AI的流式输出，将NodeOutput的文本解析出来。
    ///
    /// 用法:
    ///
    /// ```java
    /// Flux<String> message = Flux<NodeOutput>.map(nodeOutput ->
    ///         StreamingMessageUtils.handleStreamingOutput(nodeOutput, interruptionCallback, streamingFinishedCallback));
    /// ```
    ///
    /// @param output                    Agent/Graph的节点输出。
    /// @param interruptionCallback      图执行中断的回调。
    /// @param streamingFinishedCallback 流式输出结束的回调，可获取输出的全部文本。
    /// @return 输出文本。
    public static String handleStreamingOutput(@NotNull NodeOutput output,
                                               @Nullable Function<InterruptionMetadata, String> interruptionCallback,
                                               @Nullable Consumer<String> streamingFinishedCallback) {

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
                return formatOutput("");

            // 判断节点输出类型
            if (outputType == OutputType.GRAPH_NODE_STREAMING || outputType == OutputType.AGENT_MODEL_STREAMING) {
                // 流式输出
                String chunk = message.getText();
                if (chunk != null)
                    return formatOutput(chunk);
            } else if (outputType == OutputType.GRAPH_NODE_FINISHED || outputType == OutputType.AGENT_MODEL_FINISHED) {
                // 输出结束
                if (streamingFinishedCallback != null) {
                    String allMessage = message.getText(); // 全部输出
                    streamingFinishedCallback.accept(allMessage);
                }

                return formatOutput("[DONE]"); // 输出结束
            }
        }
        return formatOutput("");
    }

    private static String formatOutput(String output) {
        return output;
    }

}
