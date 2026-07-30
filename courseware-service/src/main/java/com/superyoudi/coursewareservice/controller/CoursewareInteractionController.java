package com.superyoudi.coursewareservice.controller;

import com.superyoudi.aiservice.dto.ChatMessage;
import com.superyoudi.coursewareservice.service.CoursewareInteractionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/ai/coursepreparing")
class CoursewareInteractionController {
    private final CoursewareInteractionService interactionService;

    public CoursewareInteractionController(CoursewareInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    /**
     * 聊天接口
     *
     * @param chatMessage 聊天信息
     * @return 流式输出
     * @author Sijayyx
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChat(@RequestBody ChatMessage chatMessage) {
        // TODO 使用SSE或者WebSocket让AI主动发话
        return interactionService.executeWorkflowGraph(chatMessage);
    }

    @PostMapping(value = "/chat/resume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doResume(@RequestBody ChatMessage chatMessage) {
        return interactionService.resumeWorkflowGraph(chatMessage);
    }

    // 测试接口
    @GetMapping(value = "/tchat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testChat(@RequestParam String input) {
        return interactionService.executeWorkflowGraph(new ChatMessage(1, input, null));
    }

    // 测试接口
    @GetMapping(value = "/tresume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testResme(@RequestParam String approve) { // TODO ChatMessage
        return interactionService.resumeWorkflowGraph(new ChatMessage(1, "", approve));
    }

}
