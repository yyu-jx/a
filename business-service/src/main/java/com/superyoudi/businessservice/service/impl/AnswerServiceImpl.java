package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.service.AnswerAiService;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.service.AnswerService;
import com.superyoudi.common.dto.QuestionDTO;
import com.superyoudi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerAiService answerAiService;
    private final QuestionMapper questionMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final AnswerHelpRecordService answerHelpRecordService;


    @Override
    public Flux<String> getQuestionHelpStream(Long questionId, Long taskId, Long studentId) {
        Question question = questionMapper.findById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        return answerAiService.streamQuestionSuggest(question.getContent(), question.getAnswer())
                .doOnComplete(() -> {
                    if (studentId != null) {
                        Mono.fromRunnable(() -> answerHelpRecordService.recordHelpCount(studentId, questionId, taskId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
                    }
                })
                .onErrorMap(e -> new BusinessException("AI辅导服务暂时不可用"));
    }
}
