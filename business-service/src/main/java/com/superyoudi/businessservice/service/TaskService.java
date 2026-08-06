package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.vo.ErrorQuestionVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;

import java.util.List;

public interface TaskService {

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
     * @param userId  用户id
     * @param courseId 课程id
     * @return 用户错题
     */
    List<ErrorQuestionVO> listUserErrorQuestion(Long userId, Long courseId);
}
