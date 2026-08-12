package com.superyoudi.coursewareservice.controller;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.coursewareservice.ai.node.HtmlPptGenNode;
import com.superyoudi.coursewareservice.ai.tool.HtmlPptTools;
import com.superyoudi.coursewareservice.config.GraphConfig;
import com.superyoudi.coursewareservice.pojo.dto.GenerationStatus;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import com.superyoudi.coursewareservice.service.CoursewareInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/ai/coursepreparing")
@RequiredArgsConstructor
class CoursewareInteractionController {
    private final CoursewareInteractionService interactionService;

    private final ChatModel chatModel;
    private final HtmlPptTools htmlPptTools;
    private final CoursewareGenerationService coursewareGenerationService;


    /**
     * 聊天接口
     *
     * @param userChatMessage 聊天信息
     * @return 流式输出
     * @author Sijayyx
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChat(@RequestBody UserChatMessage userChatMessage) {
        return interactionService.executeWorkflowGraph(userChatMessage);
    }

    @PostMapping(value = "/chat/resume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doResume(@RequestBody UserChatMessage userChatMessage) {
        return interactionService.resumeWorkflowGraph(userChatMessage);
    }

    @GetMapping(value = "/tppt")
    public Flux<String> testPptGeneration() {
        var pptGenerationNode = AsyncNodeAction.node_async(new HtmlPptGenNode(chatModel, htmlPptTools));

        OverAllState state = new OverAllState(Map.of(GraphConfig.KEY_PPT_OUTLINE, """
                # 测试演示文稿（3页精简版）
                
                  > **受众**：内部测试 / 项目汇报 | **时长**：约3分钟
                
                  ---
                
                  ## # 封面页
                  **标题：** 智慧办公解决方案 · 阶段性测试汇报 \s
                  **副标题：** 2026年Q2试点成果摘要 \s
                  **演讲人：** 数字化转型办公室 \s
                  **日期：** 2026年8月1日 \s
                
                  - 背景：为验证智慧办公系统的稳定性与效率提升效果，于6月启动试点 \s
                  - 目的：本次测试汇报聚焦核心功能使用率、故障率与用户满意度 \s
                  - 范围：覆盖行政、研发、财务三个部门，共计120名试点用户 \s
                  > 建议放置公司Logo、试点部门照片或系统主界面截图
                
                  ---
                
                  ## # 目录页
                  **标题：** 汇报结构 \s
                
                  - 01 | 试点概况与数据总览（关键指标一览） \s
                  - 02 | 核心功能使用分析与问题总结 \s
                  - 03 | 下一步优化计划与测试结论 \s
                  > 本页为测试版目录，正式汇报可按需扩展至5-7个章节
                
                  ---
                
                  ## # 内容页（核心数据摘要）
                  **标题：** 试点核心指标（2026.6.1 – 2026.7.31） \s
                
                  - 系统整体可用性达 **99.2%**，累计故障时间约2.5小时 \s
                  - 核心功能（会议预约、文档协作、智能审批）日均使用率 **78%** \s
                  - 用户满意度综合评分 **4.3/5.0**（样本量：96份有效问卷） \s
                  - 主要问题：审批流程超时占比 **32%**，移动端加载速度偏慢 \s
                  > 适合放置柱状图（功能使用率对比）、折线图（可用性趋势）或满意度雷达图
                
                  ---
                
                  ## # 结尾页
                  **标题：** 感谢聆听 \s
                  **下一步行动：** 基于测试数据，优化审批引擎并扩大试点范围 \s
                
                  - 已梳理Top5高频问题，技术团队将于8月中旬完成首轮修复 \s
                  - 预计9月启动第二批试点（新增销售与供应链部门） \s
                  - 欢迎各位反馈测试体验，共同完善方案 \s
                  > 可放置联系二维码、项目组邮箱或下一阶段时间轴示意图
                """, GraphConfig.KEY_COURSEWARE_ID, 1)
        );

        Map<String, Object> result = pptGenerationNode.apply(state).join();

        coursewareGenerationService.notifyCoursewareGeneratingStatus(
                GenerationStatus.success(1L, coursewareGenerationService.getPptHtml(1L), null));

        return Flux.just("success");

    }


    // 测试接口
    @GetMapping(value = "/tchat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testChat(@RequestParam String input) {
        return interactionService.executeWorkflowGraph(new UserChatMessage(1L, input, null));
    }

    // 测试接口
    @GetMapping(value = "/tresume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testResme(@RequestParam String approve) { // TODO UserChatMessage
        return interactionService.resumeWorkflowGraph(new UserChatMessage(1L, "", approve));
    }

}
