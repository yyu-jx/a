package com.superyoudi.businessservice.controller;

import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.pojo.dto.CourseDataDTO;
import com.superyoudi.businessservice.service.impl.CourseServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping
class TestController {

    private final AiTutorService aiTutorService;
    private final CourseServiceImpl courseService;

    TestController(AiTutorService aiTutorService, CourseServiceImpl courseService) {
        this.aiTutorService = aiTutorService;
        this.courseService = courseService;
    }

    @GetMapping("/test")
    public String test() {
        return "hello world!";
    }

    @GetMapping(value = "/aitutor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testCallAiTutor(@RequestParam(value = "input") String input) {
        return aiTutorService.callAiTutor(new UserChatMessage(1L, input, 1L));
    }

    @GetMapping("/kp")
    public String testCourseKnowledgePoints() throws IOException {
        String json = Files.readString(Paths.get("D:\\Desktop\\test.txt"), StandardCharsets.UTF_8);

        CourseDataDTO courseDataDTO = AiMessageUtils.readJsonValue(json, CourseDataDTO.class);

        courseService.saveCourseData(courseDataDTO);
        return "success";
    }
}
