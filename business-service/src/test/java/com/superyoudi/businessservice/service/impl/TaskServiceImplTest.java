package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.service.TaskAiService;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.dto.TaskQuestion;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.entity.Task;
import com.superyoudi.common.dto.ErrorQuestionVO;
import com.superyoudi.common.exception.BusinessException;
import com.superyoudi.common.exception.ParamInvalidException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_HOUR;
import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private StudentSubmissionMapper submissionMapper;

    @InjectMocks
    private TaskServiceImpl taskService;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private TaskAiService taskAiService;
    @Mock
    private TaskMapper taskMapper;


    @Mock
    private QuestionMapper questionMapper;

    /**
     * 场景1：参数null，抛出参数非法异常
     */
    @Test
    void submitTask_paramNull_throwParamInvalid() {
        assertThrows(ParamInvalidException.class, () -> {
            taskService.addOrUpdateStudentSubmission(null, 1L);
        });
        assertThrows(ParamInvalidException.class, () -> {
            taskService.addOrUpdateStudentSubmission(1L, null);
        });
    }

    /**
     * 场景2：已有记录，状态passed，禁止重复提交
     */
    @Test
    void submitTask_existPassedRecord_throwRepeat() {
        // mock数据库返回已提交记录
        StudentSubmission exist = new StudentSubmission();
        exist.setStatus("passed");

        when(submissionMapper.selectByTaskAndStudentId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(exist);

        assertThrows(StudentRepeatJoinCourseException.class, () -> {
            taskService.addOrUpdateStudentSubmission(100L, 200L);
        });

        // 确认没有调用更新、新增
        verify(submissionMapper, never()).update(Mockito.any());
        verify(submissionMapper, never()).insert(Mockito.any());
    }

    /**
     * 场景3：记录存在，状态不是passed，执行updateById更新
     */
    @Test
    void submitTask_existNotPassed_doUpdate() {
        StudentSubmission exist = new StudentSubmission();
        exist.setId(1L);
        exist.setStatus("unSubmit");

        when(submissionMapper.selectByTaskAndStudentId(2L, 1L))
                .thenReturn(exist);
        when(submissionMapper.update(Mockito.any())).thenReturn(1);

        taskService.addOrUpdateStudentSubmission(1L, 2L);

        // 校验调用更新，没有调用insert
        verify(submissionMapper, Mockito.times(1)).update(Mockito.any());
        verify(submissionMapper, never()).insert(Mockito.any());
    }

    /**
     * 场景4：数据库查不到记录，执行insert新增提交记录
     */
    @Test
    void submitTask_noRecord_doInsert() {
        // 查询返回null
        when(submissionMapper.selectByTaskAndStudentId(5L, 3L))
                .thenReturn(null);
        when(submissionMapper.insert(Mockito.any())).thenReturn(1);

        taskService.addOrUpdateStudentSubmission(3L, 5L);

        verify(submissionMapper, Mockito.times(1)).insert(Mockito.any());
        verify(submissionMapper, never()).update(Mockito.any());
    }


    /**
     * 场景1：Redis缓存命中，直接返回缓存，不走数据库和AI
     */
    @Test
    void diagnose_cacheHit_returnCachedValue() {
        // arrange
        Long studentId = 100L;
        String cacheKey = AI_DIAGNOSE_CACHE_KEY + studentId;
        String mockCacheText = "学生学情诊断结果：基础薄弱...";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenReturn(mockCacheText);

        // act
        String result = taskService.diagnose(studentId);

        // assert
        assertEquals(mockCacheText, result);
        // 没有查询数据库，没有调用AI，没有写缓存
        verify(taskMapper, never()).selectUserErrorQuestion(any(), any());
        verify(taskAiService, never()).diagnose(any());
        verify(valueOps, never()).set(anyString(), anyString(), anyLong(), any());
    }


    /**
     * 场景2：缓存未命中，查错题、调用AI、写入Redis缓存
     */
    @Test
    void diagnose_cacheMiss_callAi_andSetCache() {
        // arrange
        Long studentId = 101L;
        String cacheKey = AI_DIAGNOSE_CACHE_KEY + studentId;
        List<ErrorQuestionVO> mockErrorList = new ArrayList<>();
        ErrorQuestionVO vo = new ErrorQuestionVO();
        mockErrorList.add(vo);
        String mockAiResult = "AI返回学情报告内容";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        // 缓存没数据返回null
        when(valueOps.get(cacheKey)).thenReturn(null);
        when(taskMapper.selectUserErrorQuestion(eq(studentId), isNull())).thenReturn(mockErrorList);
        when(taskAiService.diagnose(mockErrorList)).thenReturn(mockAiResult);

        // act
        String result = taskService.diagnose(studentId);

        // assert
        assertEquals(mockAiResult, result);
        verify(taskMapper).selectUserErrorQuestion(studentId, null);
        verify(taskAiService).diagnose(mockErrorList);
        // 校验写入redis，过期小时数
        verify(valueOps).set(eq(cacheKey), eq(mockAiResult), eq((long) AI_DIAGNOSE_CACHE_HOUR), eq(TimeUnit.HOURS));
    }

    /**
     * 场景3：学生没有错题（错题集合为空），仍然调用AI，结果缓存
     */
    @Test
    void diagnose_noErrorQuestion_aiGetEmptyList() {
        Long studentId = 102L;
        String cacheKey = AI_DIAGNOSE_CACHE_KEY + studentId;
        List<ErrorQuestionVO> emptyList = new ArrayList<>();
        String aiResp = "该学生暂无错题记录";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenReturn(null);
        when(taskMapper.selectUserErrorQuestion(eq(studentId), isNull())).thenReturn(emptyList);
        when(taskAiService.diagnose(emptyList)).thenReturn(aiResp);

        String res = taskService.diagnose(studentId);

        assertEquals(aiResp, res);
        verify(taskAiService).diagnose(emptyList);
        verify(valueOps).set(anyString(), anyString(), anyLong(), any());
    }

    // ==================== 异常场景 ====================

    @Test
        //_taskId为null_抛出参数异常
    void getTaskQuestionList1() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> taskService.getTaskQuestionList(null));
        assertEquals("任务ID不能为空", exception.getMessage());
    }

    @Test
        //_任务不存在_抛出业务异常
    void getTaskQuestionList2() {
        // 模拟数据库查不到任务
        when(taskMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> taskService.getTaskQuestionList(999L));
        assertEquals("任务不存在", exception.getMessage());
    }

    // ==================== 正常业务场景 ====================


    // ==================== 辅助构造方法 ====================

    private Task buildMockTask(Long taskId) {
        Task task = new Task();
        task.setId(taskId);
        task.setCourseId(201L);
        task.setType("homework");
        task.setTitle("第一章单元作业");
        return task;
    }

    private TaskQuestion buildMockQuestion(Long id, String type, String answerJson) {
        TaskQuestion question = new TaskQuestion();
        question.setId(id);
        question.setTeacherId(100L);
        question.setScore(new BigDecimal("10"));
        question.setType(type);
        question.setContent("求函数极限");
        question.setAnswer(answerJson);
        return question;
    }
}