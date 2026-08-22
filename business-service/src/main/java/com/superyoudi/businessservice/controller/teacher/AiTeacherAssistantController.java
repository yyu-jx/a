package com.superyoudi.businessservice.controller.teacher;

import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.service.AiTeacherAssistantService;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/ai/teacher")
@RequiredArgsConstructor
@Slf4j
public class AiTeacherAssistantController {

    private final AiTeacherAssistantService aiTeacherAssistantService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> callAiTeacherAssistant(@RequestBody UserChatMessage<Void> message) {
        return aiTeacherAssistantService.call(message);
    }

    @GetMapping("/history/{userId}")
    public Result<ChatHistoryDTO> getChatHistory(@PathVariable Long userId) {
        return Result.success(aiTeacherAssistantService.getChatHistory(userId));
    }

}
