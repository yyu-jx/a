package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.common.dto.ErrorQuestionVO;

import java.util.List;

public interface TaskService {

    Long saveTaskData(TaskDTO taskDTO);

    /**
     * 学生提交作业
     *
     * @param userId 学生id
     * @param taskId 课程id
     */
    void submitTask(Long userId, Long taskId);

    /**
     * 获取学生作业概览
     *
     * @param userId 学生id
     * @return 学生作业概览
     */
    StudentTaskOverviewVO getOverview(Long userId);


    /**
     * 获取用户错题
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 用户错题
     */
    List<ErrorQuestionVO> listUserErrorQuestion(Long userId, Long courseId);


    /**
     * 生成ai诊断报告
     *
     * @param studentId 学生id
     * @return 诊断报告
     */
    String diagnose(Long studentId);
}


