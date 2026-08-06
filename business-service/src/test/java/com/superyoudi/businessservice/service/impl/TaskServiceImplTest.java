package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.common.exception.ParamInvalidException;
import com.superyoudi.common.exception.RepeatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private StudentSubmissionMapper submissionMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    /**
     * 场景1：参数null，抛出参数非法异常
     */
    @Test
    void submitTask_paramNull_throwParamInvalid() {
        Assertions.assertThrows(ParamInvalidException.class, () -> {
            taskService.submitTask(null, 1L);
        });
        Assertions.assertThrows(ParamInvalidException.class, () -> {
            taskService.submitTask(1L, null);
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

        Mockito.when(submissionMapper.selectByTaskAndStudentId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(exist);

        Assertions.assertThrows(RepeatException.class, () -> {
            taskService.submitTask(100L, 200L);
        });

        // 确认没有调用更新、新增
        Mockito.verify(submissionMapper, Mockito.never()).updateById(Mockito.any());
        Mockito.verify(submissionMapper, Mockito.never()).insert(Mockito.any());
    }

    /**
     * 场景3：记录存在，状态不是passed，执行updateById更新
     */
    @Test
    void submitTask_existNotPassed_doUpdate() {
        StudentSubmission exist = new StudentSubmission();
        exist.setId(1L);
        exist.setStatus("unSubmit");

        Mockito.when(submissionMapper.selectByTaskAndStudentId(2L, 1L))
                .thenReturn(exist);
        Mockito.when(submissionMapper.updateById(Mockito.any())).thenReturn(1);

        taskService.submitTask(1L,2L);

        // 校验调用更新，没有调用insert
        Mockito.verify(submissionMapper, Mockito.times(1)).updateById(Mockito.any());
        Mockito.verify(submissionMapper, Mockito.never()).insert(Mockito.any());
    }

    /**
     * 场景4：数据库查不到记录，执行insert新增提交记录
     */
    @Test
    void submitTask_noRecord_doInsert() {
        // 查询返回null
        Mockito.when(submissionMapper.selectByTaskAndStudentId(5L,3L))
                .thenReturn(null);
        Mockito.when(submissionMapper.insert(Mockito.any())).thenReturn(1);

        taskService.submitTask(3L,5L);

        Mockito.verify(submissionMapper, Mockito.times(1)).insert(Mockito.any());
        Mockito.verify(submissionMapper, Mockito.never()).updateById(Mockito.any());
    }
}