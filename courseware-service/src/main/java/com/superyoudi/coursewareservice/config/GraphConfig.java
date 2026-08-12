package com.superyoudi.coursewareservice.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.superyoudi.coursewareservice.ai.dispatcher.HumanFeedbackDispatcher;
import com.superyoudi.coursewareservice.ai.node.*;
import com.superyoudi.coursewareservice.ai.tool.HtmlPptTools;
import com.superyoudi.coursewareservice.ai.tool.HtmlWordTools;
import com.superyoudi.coursewareservice.ai.tool.RagTools;
import com.superyoudi.coursewareservice.pojo.dto.GenerationStatus;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import com.superyoudi.coursewareservice.service.CoursewareRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;


/**
 * 工作流图的配置类
 *
 * @author Sijayyx
 */
@Slf4j
@Configuration
public class GraphConfig {

    /// 用户ID
    public static final String KEY_USER_ID = "user_id";
    /// 课件生成记录ID
    public static final String KEY_COURSEWARE_ID = "courseware_id";
    /// 用户输入
    public static final String KEY_INPUT = "input";
    /// HITL的用户反馈信息（如果有）
    public static final String KEY_HITL_USER_FEEDBACK = "hitl_user_feedback";
    /// HITL的批准状态
    public static final String KEY_HITL_APPROVE_STATUS = "hitl_approve_status";
    /// 用户聊天消息的附加信息
    public static final String KEY_USER_ADDITIONAL_DATA = "additional_data";
    /// AI的流式输出
    public static final String KEY_RESPONSE = "response";
    /// 生成PPT和Word所需信息
    public static final String KEY_GENERATION_INFO = "generation_info";
    /// PPT大纲，Markdown格式
    public static final String KEY_PPT_OUTLINE = "ppt_outline";
    /// 是否可以生成PPT: TRUE | FALSE
    public static final String KEY_SHOULD_GENERATE = "should_generate";
    /// Agents的生成结果
    public static final String KEY_AGENTS_INFO = "agents_info";

//    /// word的名称
//    public static final String KEY_WORD_NAME = "word_name";
//    /// PPT的名称
//    public static final String KEY_PPT_NAME = "ppt_name";

    /// 监督者节点
    public static final String NODE_SUPERVISOR = "supervisorNode";
    /// HTML大纲生成节点
    public static final String NODE_OUTLINE_GENERATION = "htmlOutlineGenNode";
    /// HITL节点
    public static final String NODE_PPT_HITL_NODE = "pptHitlNode";
    /// PPT生成节点
    public static final String NODE_PPT_GENERATION = "pptGenerationNode";
    /// Word生成节点
    public static final String NODE_WORD_GENERATION = "wordGenerationNode";
    /// 子Agents入口节点
    public static final String NODE_SUB_AGENTS_ENTRY = "subAgentsEntryNode";
    /// 子Agents出口节点
    public static final String NODE_SUB_AGENTS_EXIT = "subAgentsExitNode";

    /// PPT子图
    public static final String SUBGRAPH_PPT_GENERATION = "pptGenSubGraph";

    private final CoursewareGenerationService coursewareGenerationService;
    private final HtmlPptTools htmlPptTools;
    private final HtmlWordTools htmlWordTools;

    public GraphConfig(CoursewareGenerationService coursewareGenerationService, HtmlPptTools htmlPptTools, HtmlWordTools htmlWordTools) {
        this.coursewareGenerationService = coursewareGenerationService;
        this.htmlPptTools = htmlPptTools;
        this.htmlWordTools = htmlWordTools;
    }

    @Bean
    public MessageWindowChatMemory chatMemory() {
        // 默认存储最近的 30 条消息
        return MessageWindowChatMemory.builder().maxMessages(30).build();
    }

    /// 注册并配置工作流图的Bean
    ///
    /// 这个图定义了AI的工作流程。
    ///
    /// 图内包含以下节点：
    /// - `supervisorNode`: 监督者节点，用于处理用户输入并生成AI的流式输出。
    /// - `pptGenerationNode`: PPT生成节点，用于生成PPT。异步执行。
    /// - `wordGenerationNode`: Word生成节点，用于生成Word。异步执行。
    /// - `subAgentsEntryNode`: 子Agents入口节点，提供子Agents异步执行的入口
    /// - `subAgentsExitNode`: 子Agents出口节点，用于汇总子Agents的输出。
    ///
    /// 流程介绍：
    /// 用户的输入会从START被发送给`supervisorNode`进行处理，并生成AI的流式输出。
    /// <p>
    /// 如果`supervisorNode`决定执行PPT与Word生成的请求，它会生成一个JSON`KEY_GENERATION_INFO`，并将`KEY_READY_TO_GENERATE`设置为TRUE，
    /// 图会根据状态将数据传递给`pptGenerationNode`和`wordGenerationNode`。它们会将各自的生成结果（成功/失败）传入`KEY_AGENTS_INFO`中。
    /// 当子节点执行完毕后，会将数据再次传递给`supervisorNode`，它会根据执行的结果，对用户做出响应。
    /// <p>
    /// 如果`supervisorNode`暂时不生成PPT或Word，则会将`KEY_READY_TO_GENERATE`设置为FALSE，随后执行到END节点。
    ///
    /// @param chatClient 聊天客户端，自动注入
    /// @return 工作流图的Bean
    /// @author Sijayyx
    @Bean("coursewareGraph")
    public CompiledGraph coursewareGraph(ChatClient.Builder chatClient, ChatModel chatModel, CoursewareRecordService coursewareRecordService) throws GraphStateException {

        log.info("正在初始化 CoursewareGraph。");

        KeyStrategyFactory keyStrategyFactory = () -> Map.ofEntries(
                Map.entry(KEY_USER_ID, KeyStrategy.REPLACE),
                Map.entry(KEY_COURSEWARE_ID, KeyStrategy.REPLACE),
                Map.entry(KEY_INPUT, KeyStrategy.REPLACE),
                Map.entry(KEY_RESPONSE, KeyStrategy.APPEND),
                Map.entry(KEY_USER_ADDITIONAL_DATA, KeyStrategy.REPLACE),
                Map.entry(KEY_GENERATION_INFO, KeyStrategy.REPLACE),
                Map.entry(KEY_SHOULD_GENERATE, KeyStrategy.REPLACE),
                Map.entry(KEY_AGENTS_INFO, KeyStrategy.APPEND),
                Map.entry(KEY_PPT_OUTLINE, KeyStrategy.REPLACE),
                Map.entry(KEY_HITL_USER_FEEDBACK, KeyStrategy.REPLACE),
                Map.entry(KEY_HITL_APPROVE_STATUS, KeyStrategy.REPLACE)
        );

        // 创建 rag 工具
        RagTools ragTools = new RagTools(coursewareGenerationService);

        // 创建节点
        var supervisorNode = AsyncNodeAction.node_async(new SupervisorNode(chatModel, chatClient, ragTools));

        var pptOutlineGenNode = AsyncNodeAction.node_async(new PptOutlineGenNode(chatModel));
        var pptHitlNode = new HumanFeedbackNode();

        var pptGenerationNode = AsyncNodeAction.node_async(new HtmlPptGenNode(chatModel, htmlPptTools));
        var wordGenerationNode = AsyncNodeAction.node_async(new HtmlWordGenNode(chatModel, htmlWordTools));


        var subAgentsEntryNode = AsyncNodeAction.node_async(state -> {
            log.info("子Agents入口节点执行");

            // 清除AGENTS_INFO和用户输入
            state.updateState(Map.of(
                    KEY_AGENTS_INFO, List.of(),
                    KEY_INPUT, ""
            ));

            String generationInfo = state.value(KEY_GENERATION_INFO, "错误");
            return Map.of(KEY_GENERATION_INFO, generationInfo);
        });

        var subAgentsExitNode = AsyncNodeAction.node_async(
                state -> {
                    log.info("子Agents出口节点执行");

                    // 生成完成，将HTML发送到前端
                    Long coursewareId = state.value(KEY_COURSEWARE_ID, Long.class).orElse(null);
                    String pptHtml = coursewareGenerationService.getPptHtml(coursewareId);
                    String wordHtml = coursewareGenerationService.getWordHtml(coursewareId);
                    coursewareGenerationService
                            .notifyCoursewareGeneratingStatus(GenerationStatus.success(coursewareId, pptHtml, wordHtml));

                    // 存表
                    coursewareRecordService.saveHtmlColumns(coursewareId, pptHtml, wordHtml);

                    return Map.of(KEY_AGENTS_INFO, state.value(KEY_AGENTS_INFO, "成功"));
                }
        );

        // PPT生成子图
        CompiledGraph pptGenSubgraph = new StateGraph("pptGenSubgraph", keyStrategyFactory)
                .addNode(NODE_PPT_GENERATION, pptGenerationNode)
                .addNode(NODE_OUTLINE_GENERATION, pptOutlineGenNode)
                .addNode(NODE_PPT_HITL_NODE, pptHitlNode)
                .addEdge(StateGraph.START, NODE_OUTLINE_GENERATION)
                .addEdge(NODE_OUTLINE_GENERATION, NODE_PPT_HITL_NODE)
                .addConditionalEdges(NODE_PPT_HITL_NODE,
                        // 处理 HITL
                        AsyncEdgeAction.edge_async(
                                new HumanFeedbackDispatcher(
                                        NODE_PPT_GENERATION, NODE_OUTLINE_GENERATION, StateGraph.END)),
                        Map.of(NODE_PPT_GENERATION, NODE_PPT_GENERATION,
                                NODE_OUTLINE_GENERATION, NODE_OUTLINE_GENERATION,
                                StateGraph.END, StateGraph.END)
                )
                .addEdge(NODE_PPT_GENERATION, StateGraph.END)
                .compile();

        StateGraph stateGraph = new StateGraph("workflowGraph", keyStrategyFactory)
                .addNode(NODE_SUPERVISOR, supervisorNode)
                .addNode(SUBGRAPH_PPT_GENERATION, pptGenSubgraph) // 使用子图代替单独节点执行
                .addNode(NODE_WORD_GENERATION, wordGenerationNode)
                .addNode(NODE_SUB_AGENTS_ENTRY, subAgentsEntryNode)
                .addNode(NODE_SUB_AGENTS_EXIT, subAgentsExitNode)
                // 开始执行
                .addEdge(StateGraph.START, NODE_SUPERVISOR)   // 监督者节点是否控制子节点执行？
                .addConditionalEdges(NODE_SUPERVISOR,
                        AsyncEdgeAction.edge_async(
                                state -> state.value(KEY_SHOULD_GENERATE, "FALSE")
                        ), Map.of("TRUE", NODE_SUB_AGENTS_ENTRY,
                                "FALSE", StateGraph.END)
                )
                // 进入子图执行
                .addEdge(NODE_SUB_AGENTS_ENTRY, SUBGRAPH_PPT_GENERATION)
                .addEdge(NODE_SUB_AGENTS_ENTRY, NODE_WORD_GENERATION)
                .addEdge(NODE_WORD_GENERATION, NODE_SUB_AGENTS_EXIT)
                .addEdge(SUBGRAPH_PPT_GENERATION, NODE_SUB_AGENTS_EXIT)
                .addEdge(NODE_SUB_AGENTS_EXIT, StateGraph.END);

        CompiledGraph graph = stateGraph.compile();
        log.info("CoursewareGraph 初始化完成。");

        return graph;
    }
}
