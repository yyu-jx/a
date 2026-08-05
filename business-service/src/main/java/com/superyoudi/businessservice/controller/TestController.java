package com.superyoudi.businessservice.controller;

import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.service.AiTutorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping
class TestController {

    private final AiTutorService aiTutorService;

    TestController(AiTutorService aiTutorService) {
        this.aiTutorService = aiTutorService;
    }

    @GetMapping("/test")
    public String test() {
        return "hello world!";
    }

    @GetMapping(value = "/aitutor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testCallAiTutor(@RequestParam(value = "input") String input) {
        return aiTutorService.callAiTutor(new UserChatMessage(1L, input, 1L));
    }
}
