package com.superyoudi.businessservice.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.superyoudi.aiservice.service.TaskAiService;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.entity.Task;
import com.superyoudi.businessservice.pojo.entity.TaskList;
import com.superyoudi.businessservice.pojo.entity.TaskQuestion;
import com.superyoudi.businessservice.pojo.vo.QuestionItemVO;
import com.superyoudi.businessservice.pojo.vo.QuestionOptionVO;
import com.superyoudi.businessservice.pojo.vo.TaskQuestionVO;
import com.superyoudi.common.dto.ErrorQuestionVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.exception.BusinessException;
import com.superyoudi.common.exception.ErrorQuestionNotFoundException;
import com.superyoudi.common.exception.ParamInvalidException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_HOUR;
import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_KEY;


@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final StudentSubmissionMapper studentSubmissionMapper;
    private final TaskMapper taskMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;
    private final TaskAiService taskAiService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;

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

    @Override
    public String diagnose(Long studentId) {
        String cacheKey = AI_DIAGNOSE_CACHE_KEY + studentId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Long courseId = null;
        List<ErrorQuestionVO> allErrorQuestionVO = taskMapper.selectUserErrorQuestion(studentId, courseId);

        String result = taskAiService.diagnose(allErrorQuestionVO);

        stringRedisTemplate.opsForValue().set(cacheKey, result, AI_DIAGNOSE_CACHE_HOUR, TimeUnit.HOURS);
        return result;
    }

    @Override
    public Page<TaskList> getTaskPage(Long userId, Long courseId, Integer pageNum, Integer pageSize, String status) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 判断角色
        String role = userMapper.selectUserRoleById(userId);
        if (role == null || role.isBlank()) {
            throw new BusinessException("用户不存在");
        }

        int realPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int realPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        Pageable pageable = PageRequest.of(realPageNum - 1, realPageSize);
        if("teacher".equals(role)){
            // 教师：查自己布置的全部任务
            return taskMapper.selectTeacherTaskPage(pageable, userId, courseId, status);
        }else{
            // 学生：只查已发布published分配给自己的任务
            return taskMapper.selectStudentTaskPage(pageable, userId, courseId, status);
        }
    }

    /**
     * 获取任务题目列表
     *
     * @param taskId 任务id
     * @return 任务问题列表
     */
    @Override
    public TaskQuestionVO getTaskQuestionList(Long taskId) {
        // 1. 校验参数
        if (taskId == null) {
            throw new BusinessException("任务ID不能为空");
        }

        // 2. 查询任务基本信息
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 3. 查询任务关联的所有题目（按题号排序）
        List<TaskQuestion> questionList = questionMapper.selectByTaskId(taskId);

        // 4. 组装题目VO，解析JSON提取选项
        List<QuestionItemVO> itemVOList = questionList.stream()
                .map(q -> {
                    QuestionItemVO vo = new QuestionItemVO();
                    vo.setId(q.getId());
                    vo.setTeacherId(q.getTeacherId());
                    vo.setScore(q.getScore());
                    vo.setType(q.getType());
                    vo.setContent(q.getContent());
                    // 解析answer JSON，提取options作为选项返回
                    vo.setAnswer(parseOptionsFromAnswerJson(q.getAnswer()));
                    return vo;
                })
                .collect(Collectors.toList());

        // 5. 组装外层返回
        TaskQuestionVO result = new TaskQuestionVO();
        result.setCourseId(task.getCourseId());
        result.setTaskType(task.getType());
        result.setTaskTitle(task.getTitle());
        result.setQuestionList(itemVOList);
        return result;

    }

    /**
     * 从题目answer JSON中提取选项列表
     * 选择题/判断题返回options；填空、主观题返回空数组
     */
    private List<QuestionOptionVO> parseOptionsFromAnswerJson(String answerJson) {
        if (StrUtil.isBlank(answerJson)) {
            return Collections.emptyList();
        }

        try {
            JSONObject answerObj = JSONUtil.parseObj(answerJson);
            // 没有options字段（填空、主观题）直接返回空
            if (!answerObj.containsKey("options")) {
                return Collections.emptyList();
            }

            // 流式遍历选项，转成VO列表
            return answerObj.getJSONArray("options")
                    .stream()
                    .map(item -> {
                        JSONObject opt = (JSONObject) item;
                        QuestionOptionVO vo = new QuestionOptionVO();
                        vo.setKey(opt.getStr("key"));
                        vo.setText(opt.getStr("text"));
                        return vo;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
