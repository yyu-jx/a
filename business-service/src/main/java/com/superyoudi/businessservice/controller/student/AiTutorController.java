package com.superyoudi.businessservice.controller.student;

import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/student")
public class AiTutorController {

    private final AiTutorService aiTutorService;

    @GetMapping("/history/{studentId}")
    public Result<ChatHistoryDTO> getChatHistory(@PathVariable Long studentId,
                                                 @RequestParam Long courseId) {
        return Result.success(aiTutorService.getChatHistory(studentId, courseId));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody UserChatMessage<Long> message) {
        return aiTutorService.call(message);
    }
}
