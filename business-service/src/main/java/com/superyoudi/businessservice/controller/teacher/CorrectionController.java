package com.superyoudi.businessservice.controller.teacher;


import com.superyoudi.businessservice.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CorrectionController {

    private final AnswerService answerService;

    @GetMapping("/ai/suggestions/correcting")
    public Flux<String> getCorrectionSuggestions(Long questionId, Long taskId,Long studentId) {
        return answerService.getCorrectionSuggestions(questionId, taskId, studentId);
    }
}
