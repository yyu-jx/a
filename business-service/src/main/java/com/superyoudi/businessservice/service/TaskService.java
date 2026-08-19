package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.entity.TaskList;
import com.superyoudi.businessservice.pojo.vo.StudentAnswerVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.pojo.vo.TaskQuestionVO;
import com.superyoudi.common.dto.ErrorQuestionVO;
import org.springframework.data.domain.Page;

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


    /**
     * 获取任务列表
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param status   状态
     * @return 任务列表
     */
    Page<TaskList> getTaskPage(Long userId, Long courseId, Integer pageNum, Integer pageSize, String status);

    /**
     * 获取任务题目列表
     *
     * @param taskId 任务id
     * @return 任务问题列表
     */
    TaskQuestionVO getTaskQuestionList(Long taskId);


    /**
     * 获取学生单题答题记录（批改用）
     *
     * @param studentId    学生ID（可选）
     * @param submissionId 提交表ID（可选）
     * @return 答题记录列表
     */
    List<StudentAnswerVO> getStudentAnswers(Long studentId, Long submissionId);

    /**
     * 修改学生单题答题记录（批改打分）
     *
     * @param dto 答题记录
     */
    void updateStudentAnswer(StudentAnswerVO dto);

    /**
     * 获取所有学生的提交表（批改用）
     *
     * @param taskId 任务ID（可选）
     * @return 提交记录列表
     */
    List<StudentSubmission> getSubmissions(Long taskId);

    public void addPathNodeTaskRel(Long taskId, Long pathNodeId, Long studentId);

    void removePathNodeTaskRel(Long taskId, Long pathNodeId);

    public StudentSubmission tryGetTaskSubmissionByPathNodeId(Long pathNodeId, Long studentId);
}
