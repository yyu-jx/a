package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.service.AnswerAiService;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.common.dto.QuestionDTO;
import com.superyoudi.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

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

    @InjectMocks
    private AnswerServiceImpl questionHelpService;

    // ---------------- getQuestionHelpStream 测试 ----------------

    /**
     * 场景1：题目不存在，抛出BusinessException
     */
    @Test
    void getQuestionHelpStream_questionNotFound_throwException() {
        Long qId = 1L;
        Long tId = 2L;
        Long sId = 3L;
        when(questionMapper.selectById(qId)).thenReturn(null);

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

        when(questionMapper.selectById(qId)).thenReturn(dto);
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

        when(questionMapper.selectById(qId)).thenReturn(dto);
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
        helpRecordService.recordHelpCount(null, 1L,2L);
        helpRecordService.recordHelpCount(1L,null,2L);
        helpRecordService.recordHelpCount(1L,2L,null);

        verify(studentSubmissionMapper,never()).selectByTaskAndStudentId(any(),any());
        verify(studentAnswerMapper,never()).selectAnswerBySubQid(any(),any());
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
        when(studentSubmissionMapper.selectByTaskAndStudentId(tId,sId)).thenReturn(null);
        // mock插入后返回id，MyBatis insert回填id
        StudentSubmission mockInsertedSub = new StudentSubmission();
        mockInsertedSub.setId(888L);
        when(studentSubmissionMapper.insert(any(StudentSubmission.class))).thenAnswer(i->{
            StudentSubmission arg = i.getArgument(0);
            arg.setId(888L);
            return 1;
        });
        // 根据subId+questionId查答题记录返回null
        when(studentAnswerMapper.selectAnswerBySubQid(888L,qId)).thenReturn(null);

        helpRecordService.recordHelpCount(sId,qId,tId);

        verify(studentSubmissionMapper).insert(any(StudentSubmission.class));
        verify(studentAnswerMapper).insertEmptyAnswerRecord(eq(888L),eq(qId),eq(1L));
        verify(studentAnswerMapper,never()).incrementHelpCount(any());
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
        when(studentSubmissionMapper.selectByTaskAndStudentId(tId,sId)).thenReturn(existSub);

        StudentAnswer existAnswer = new StudentAnswer();
        existAnswer.setId(555L);
        when(studentAnswerMapper.selectAnswerBySubQid(subId,qId)).thenReturn(existAnswer);

        helpRecordService.recordHelpCount(sId,qId,tId);


        verify(studentSubmissionMapper, never()).insert(any(StudentSubmission.class));

        verify(studentAnswerMapper).incrementHelpCount(eq(555L));


        verify(studentAnswerMapper, never()).insertEmptyAnswerRecord(anyLong(), anyLong(), anyLong());
    }
}

