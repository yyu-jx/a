package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.vo.ErrorQuestionVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.exception.ErrorQuestionNotFoundException;
import com.superyoudi.common.exception.ParamInvalidException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final StudentSubmissionMapper studentSubmissionMapper;
    private final TaskMapper taskMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;

    /**
     * 提交任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     */
    @Override
    public void submitTask(Long userId, Long taskId) {
        //1.参数校验
        if(userId == null || taskId == null){
            throw new ParamInvalidException("参数不能为空");
        }

        //2.查询该学生该任务的提交记录
        StudentSubmission existRecord = studentSubmissionMapper.selectByTaskAndStudentId(taskId, userId);

        LocalDateTime now = LocalDateTime.now();

        if(existRecord != null){
            //记录已存在
            // 已经批改完毕，禁止提交
            if("passed".equals(existRecord.getStatus())){
                throw new StudentRepeatJoinCourseException("已提交，不可重复提交");
            }
            //更新状态为已提交，更新提交时间；分数、评语、批改时间保留原值
            studentSubmissionMapper.updateById(existRecord);
        }else{
            //3.不存在记录：新增提交记录
            StudentSubmission newRecord = new StudentSubmission();
            newRecord.setTaskId(taskId);
            newRecord.setStudentId(userId);
            newRecord.setStatus("submitted");
            newRecord.setSubmittedTime(now);
            newRecord.setCreatedAt(now);
            studentSubmissionMapper.insert(newRecord);
        }
    }

    /**
     * 获取任务概览
     *
     * @param userId 用户ID
     * @return 任务概览
     */
    @Override
    public StudentTaskOverviewVO getOverview(Long userId) {
        StudentTaskOverviewVO vo = new StudentTaskOverviewVO();

        //获取已完成任务数量
        Integer finishedCount = studentSubmissionMapper.countFinishedTask(userId);
        vo.setFinishedTaskCount(finishedCount);

        Integer draftCount = studentSubmissionMapper.countDraftTask(userId);
        Integer allJoinTaskCount = taskMapper.countStudentAllTask(userId);
        Integer todo = (allJoinTaskCount - finishedCount) + draftCount;
        vo.setTodoTaskCount(todo);

        Double taskScore=studentCourseProgressMapper.getAvgTaskScore(userId);
        vo.setAverageAccuracy(taskScore/100);

        return vo;
    }

    @Override
    public List<ErrorQuestionVO> listUserErrorQuestion(Long userId, Long courseId) {
        List<ErrorQuestionVO> errorQuestionVO = taskMapper.selectUserErrorQuestion(userId, courseId);

        if(CollectionUtils.isEmpty(errorQuestionVO)){
            throw  new ErrorQuestionNotFoundException("没有错题");
        }

        return errorQuestionVO;
    }

}
