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
import com.superyoudi.common.dto.QuestionDTO;
import com.superyoudi.common.dto.TeacherAiCorrectingDTO;
import com.superyoudi.common.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnswerServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private AnswerAiService answerAiService;
    @Mock
    private AnswerHelpRecordService answerHelpRecordService;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TaskQuestionRelMapper taskQuestionMapper;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AnswerServiceImpl questionHelpService;


    private final Long questionId = 1001L;
    private final Long taskId = 2001L;
    private final Long studentId = 3001L;
    private final String cacheKey = "ai:correct:" + questionId + ":" + taskId + ":" + studentId;


    // ---------------- getQuestionHelpStream 测试 ----------------

    /**
     * 场景1：题目不存在，抛出BusinessException
     */
    @Test
    void getQuestionHelpStream_questionNotFound_throwException() {
        Long qId = 1L;
        Long tId = 2L;
        Long sId = 3L;
        when(questionMapper.findById(qId)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> questionHelpService.getQuestionHelpStream(qId, tId, sId));
        assertEquals("题目不存在", ex.getMessage());

        verify(answerAiService, never()).streamQuestionSuggest(any(), any());
    }

    /**
     * 场景2：正常获取AI流，Flux完成后，异步调用记录求助次数
     */
    @Test
    void getQuestionHelpStream_normalComplete_triggerRecordHelp() {
        Long qId = 10L;
        Long tId = 20L;
        Long sId = 30L;
        QuestionDTO dto = new QuestionDTO();
        dto.setContent("题目内容");
        dto.setAnswer("参考答案");

        when(questionMapper.findById(qId)).thenReturn(null);
        Flux<String> mockAiFlux = Flux.just("片段1", "片段2", "片段3");
        when(answerAiService.streamQuestionSuggest(dto.getContent(), dto.getAnswer())).thenReturn(mockAiFlux);

        Flux<String> resultFlux = questionHelpService.getQuestionHelpStream(qId, tId, sId);

        List<String> results = resultFlux.collectList().block();
        assertNotNull(results);
        assertEquals(List.of("片段1", "片段2", "片段3"), results);

        verify(answerHelpRecordService, times(1)).recordHelpCount(sId, qId, tId);
    }

    /**
     * 场景3：AI流发生异常，走onErrorMap，**不会执行doOnComplete（不会统计求助）**
     */
    @Test
    void getQuestionHelpStream_aiError_noRecordCount() {
        Long qId = 11L;
        Long tId = 21L;
        Long sId = 31L;
        QuestionDTO dto = new QuestionDTO();
        dto.setContent("题目");
        dto.setAnswer("答案");

        when(questionMapper.findById(qId)).thenReturn(null);
        Flux<String> errorFlux = Flux.error(new RuntimeException("大模型调用失败"));
        when(answerAiService.streamQuestionSuggest(any(), any())).thenReturn(errorFlux);

        Flux<String> flux = questionHelpService.getQuestionHelpStream(qId, tId, sId);

        Exception e = assertThrows(Exception.class, () -> flux.collectList().block());
        Throwable cause = e instanceof BusinessException ? e : e.getCause();
        assertInstanceOf(BusinessException.class, cause);
        assertEquals("AI辅导服务暂时不可用", cause.getMessage());

        verify(answerHelpRecordService, never()).recordHelpCount(any(), any(), any());
    }

    // ---------------- recordHelpCount 单元测试 ----------------
    @Mock
    private StudentSubmissionMapper studentSubmissionMapper;
    @Mock
    private StudentAnswerMapper studentAnswerMapper;

    @InjectMocks
    private AnswerHelpRecordService helpRecordService;

    /**
     * 场景A：入参任意一个null，直接return，不操作数据库
     */
    @Test
    void recordHelpCount_paramNull_returnNothing() {
        helpRecordService.recordHelpCount(null, 1L, 2L);
        helpRecordService.recordHelpCount(1L, null, 2L);
        helpRecordService.recordHelpCount(1L, 2L, null);

        verify(studentSubmissionMapper, never()).selectByTaskAndStudentId(any(), any());
        verify(studentAnswerMapper, never()).selectAnswerBySubQid(any(), any());
    }

    /**
     * 场景B：没有提交记录 → 新建draft提交记录，没有答题记录 → 插入空答题记录
     */
    @Test
    void recordHelpCount_noSubmission_createDraft_andInsertEmptyAnswer() {
        Long sId = 100L;
        Long qId = 200L;
        Long tId = 300L;

        // 查询提交记录返回null
        when(studentSubmissionMapper.selectByTaskAndStudentId(tId, sId)).thenReturn(null);
        // mock插入后返回id，MyBatis insert回填id
        StudentSubmission mockInsertedSub = new StudentSubmission();
        mockInsertedSub.setId(888L);
        when(studentSubmissionMapper.insert(any(StudentSubmission.class))).thenAnswer(i -> {
            StudentSubmission arg = i.getArgument(0);
            arg.setId(888L);
            return 1;
        });
        // 根据subId+questionId查答题记录返回null
        when(studentAnswerMapper.selectAnswerBySubQid(888L, qId)).thenReturn(null);

        helpRecordService.recordHelpCount(sId, qId, tId);

        verify(studentSubmissionMapper).insert(any(StudentSubmission.class));
        verify(studentAnswerMapper).insertEmptyAnswerRecord(eq(888L), eq(qId), eq(1L));
        verify(studentAnswerMapper, never()).incrementHelpCount(any());
    }

    /**
     * 场景C：已有提交记录，并且已有答题记录，执行求助次数+1
     */
    @Test
    void recordHelpCount_hasSubmission_hasAnswer_incrementHelp() {
        Long sId = 101L;
        Long qId = 201L;
        Long tId = 301L;
        Long subId = 999L;

        StudentSubmission existSub = new StudentSubmission();
        existSub.setId(subId);
        when(studentSubmissionMapper.selectByTaskAndStudentId(tId, sId)).thenReturn(existSub);

        StudentAnswer existAnswer = new StudentAnswer();
        existAnswer.setId(555L);
        when(studentAnswerMapper.selectAnswerBySubQid(subId, qId)).thenReturn(existAnswer);

        helpRecordService.recordHelpCount(sId, qId, tId);


        verify(studentSubmissionMapper, never()).insert(any(StudentSubmission.class));

        verify(studentAnswerMapper).incrementHelpCount(eq(555L));


        verify(studentAnswerMapper, never()).insertEmptyAnswerRecord(anyLong(), anyLong(), anyLong());
    }


    /**
     * 场景2：Redis缓存命中，直接返回流式文本，不访问数据库、不调用AI
     */
    @Test
    void getCorrectionSuggestions_cacheHit() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // mock缓存存在结果
        when(valueOperations.get(cacheKey)).thenReturn("这道题建议重点掌握知识点");

        Flux<String> flux = questionHelpService.getCorrectionSuggestions(questionId, taskId, studentId);
        List<String> resultList = flux.collectList().block();

        // 把list里面所有片段拼接回完整文本
        String fullText = String.join("", resultList);

        // JUnit5原生断言，不需要assertThat
        Assertions.assertNotNull(resultList);
        Assertions.assertTrue(fullText.contains("这道题建议重点掌握知识点"));

        // 缓存命中：不走数据库、不走AI
        verify(studentSubmissionMapper, Mockito.never()).selectByTaskAndStudentId(any(), any());
        verify(answerAiService, Mockito.never()).getCorrectionSuggestions(any());
    }

    /**
     * 场景3：缓存未命中，正常完整流程，调用AI，存入Redis
     */
    @Test
    void getCorrectionSuggestions_noCache_normalFlow() throws Exception {
        // redis无缓存
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);

        // mock数据库数据
        StudentSubmission submission = new StudentSubmission();
        submission.setId(5001L);
        when(studentSubmissionMapper.selectByTaskAndStudentId(taskId, studentId)).thenReturn(submission);

        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setAnswer("{\"content\":\"学生作答内容\"}");
        when(studentAnswerMapper.getAnswerBySubQid(submission.getId(), questionId)).thenReturn(studentAnswer);

        Question question = new Question();
        question.setId(questionId);
        question.setType("subjective");
        question.setAnswer("{\"answer\":\"标准答案\",\"score_key\":[\"关键点1\",\"关键点2\"]}");
        when(questionMapper.findById(questionId)).thenReturn(question);

        TaskQuestionRel rel = new TaskQuestionRel();
        rel.setScore(10);
        when(taskQuestionMapper.findByTaskIdAndQuestionId(taskId, questionId)).thenReturn(rel);

        // mock JSON解析
        when(objectMapper.readValue(eq(question.getAnswer()), any(TypeReference.class)))
                .thenReturn(Map.of("answer", "标准答案", "score_key", List.of("关键点1", "关键点2")));
        when(objectMapper.readValue(eq(studentAnswer.getAnswer()), any(TypeReference.class)))
                .thenReturn(Map.of("content", "学生作答内容"));

        // mock AI返回流
        Flux<String> aiMockFlux = Flux.just("AI", "批改", "结果");
        when(answerAiService.getCorrectionSuggestions(any(TeacherAiCorrectingDTO.class))).thenReturn(aiMockFlux);

        Flux<String> resultFlux = questionHelpService.getCorrectionSuggestions(questionId, taskId, studentId);

        // 替换StepVerifier
        List<String> resultList = resultFlux.collectList().block();
        Assertions.assertNotNull(resultList);

        // 重点：不要断言分片size和下标！只断言拼接之后完整字符串
        String fullText = String.join("", resultList);
        Assertions.assertEquals("AI批改结果", fullText);

        // 校验：缓存写入一次，存入拼接完成的完整字符串
        verify(valueOperations, times(1))
                .set(eq(cacheKey), eq("AI批改结果"), anyLong(), any());

        // 校验数据库、AI服务被调用
        verify(studentSubmissionMapper).selectByTaskAndStudentId(any(), any());
        verify(answerAiService).getCorrectionSuggestions(any(TeacherAiCorrectingDTO.class));
    }
}

