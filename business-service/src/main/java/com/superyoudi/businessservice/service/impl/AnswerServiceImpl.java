package com.superyoudi.businessservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.service.AnswerAiService;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskQuestionRelMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.entity.TaskQuestionRel;
import com.superyoudi.businessservice.service.AnswerService;
import com.superyoudi.common.dto.QuestionDTO;
import com.superyoudi.common.dto.TeacherAiCorrectingDTO;
import com.superyoudi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerAiService answerAiService;
    private final QuestionMapper questionMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final AnswerHelpRecordService answerHelpRecordService;
    private final TaskQuestionRelMapper taskQuestionMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;



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

    @Override
    public Flux<String> getCorrectionSuggestions(Long questionId, Long taskId,Long studentId) {
        // 参数校验
        if (taskId == null || questionId == null) {
            throw new BusinessException("taskId、questionId不能为空");
        }

        String cacheKey = "ai:correct:" + questionId + ":" + taskId + ":" + studentId;

        // 1.先查Redis缓存
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，把完整字符串按字符转成流式返回给SSE
            return Flux.fromArray(cacheValue.split(""));
        }

        // 3. 查询该学生在该任务下的最新提交记录
        StudentSubmission submission = studentSubmissionMapper.selectByTaskAndStudentId(taskId, studentId);
        if (submission == null) {
            throw new BusinessException("该学生暂无此任务的提交记录");
        }
        Long submissionId = submission.getId();

        // 4. 查询这道题的学生作答详情
        StudentAnswer studentAnswer = studentAnswerMapper.getAnswerBySubQid(submissionId, questionId);
        if (studentAnswer == null) {
            throw new BusinessException("未找到该题的学生作答记录");
        }

        // 5. 查询题目基础信息
        Question question = questionMapper.findById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        // 6. 仅主观题支持AI批改
        if (!"subjective".equals(question.getType())) {
            throw new BusinessException("仅主观题支持AI批改建议");
        }

        // 7. 查询任务-题目关联，获取本题满分分值
        TaskQuestionRel taskQuestionRel = taskQuestionMapper.findByTaskIdAndQuestionId(taskId, questionId);
        if (taskQuestionRel == null) {
            throw new BusinessException("该题目不在当前任务中");
        }
        Integer fullScore = taskQuestionRel.getScore();

        // 8. 解析题目答案JSON：提取标准答案 + 得分关键点
        Map<String, Object> answerMap;
        try {
            answerMap = objectMapper.readValue(question.getAnswer(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("题目answer字段JSON解析失败，questionId:{}", questionId, e);
            throw new BusinessException("题目答案数据格式异常");
        }
        String standardAnswer = (String) answerMap.get("answer");
        List<String> scoreKeyList = (List<String>) answerMap.get("score_key");

        // 9. 解析学生作答JSON：提取学生答题内容
        Map<String, Object> stuAnswerMap;
        try {
            stuAnswerMap = objectMapper.readValue(studentAnswer.getAnswer(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("学生作答JSON解析失败，submissionId:{}, questionId:{}", submission.getId(), questionId, e);
            throw new BusinessException("学生作答数据格式异常");
        }
        String studentAnswerContent = (String) stuAnswerMap.get("content");

        // 10. 组装最终DTO，返回给AI模块
        TeacherAiCorrectingDTO dto = new TeacherAiCorrectingDTO();
        dto.setQuestionContent(question.getContent());
        dto.setStandardAnswer(standardAnswer);
        dto.setScoreKeyList(scoreKeyList);
        dto.setStudentAnswerContent(studentAnswerContent);
        dto.setFullScore(fullScore);

        // 11. AI获取流式结果，收集完整结果存入Redis，再继续流式下发
        Flux<String> aiFlux = answerAiService.getCorrectionSuggestions(dto)
                .onErrorMap( e -> new BusinessException("AI批改服务暂时不可用"));

        // 12. collectList 把AI全部字符收集拼接完整字符串
        return aiFlux
                .collectList()
                .map(list -> {
                    String fullText = String.join("", list);
                    // 设置缓存，过期时间 2小时，可自己改
                    stringRedisTemplate.opsForValue().set(cacheKey, fullText, 6L, TimeUnit.HOURS);
                    return fullText;
                })
                .flatMapMany(full -> Flux.fromArray(full.split("")));
    }

}
