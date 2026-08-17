package com.superyoudi.businessservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.service.TaskAiService;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.mapper.TaskQuestionRelMapper;
import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.pojo.entity.Task;
import com.superyoudi.businessservice.pojo.entity.TaskQuestionRel;
import com.superyoudi.businessservice.pojo.vo.StudentAnswerVO;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.dto.ErrorQuestionVO;
import com.superyoudi.common.exception.ErrorQuestionNotFoundException;
import com.superyoudi.common.exception.ParamInvalidException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_HOUR;
import static com.superyoudi.common.constants.RedisConstants.AI_DIAGNOSE_CACHE_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final TaskMapper taskMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;
    private final TaskAiService taskAiService;
    private final StringRedisTemplate stringRedisTemplate;
    private final TaskQuestionRelMapper taskQuestionRelMapper;
    private final ObjectMapper objectMapper;

    /**
     * 更新任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTaskData(TaskDTO taskDTO) {
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);

        if (task.getId() == null)
            taskMapper.insert(task);
        else
            taskMapper.update(task);

        // 删除旧的关联
        taskQuestionRelMapper.deleteAllByTaskId(task.getId());

        // 构造新的关联数据
        List<TaskQuestionRel> list = taskDTO.getQuestionRelInfos().stream()
                .map(rel -> TaskQuestionRel.builder()
                        .id(null)
                        .taskId(task.getId())
                        .questionId(rel.getQuestionId())
                        .score(rel.getScore())
                        .questionSort(rel.getQuestionSort())
                        .build())
                .toList();

        // 插入新关联
        taskQuestionRelMapper.batchInsert(list);

        return task.getId();
    }


    /**
     * 提交任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     */
    @Override
    public void submitTask(Long userId, Long taskId) {
        //1.参数校验
        if (userId == null || taskId == null) {
            throw new ParamInvalidException("参数不能为空");
        }

        //2.查询该学生该任务的提交记录
        StudentSubmission existRecord = studentSubmissionMapper.selectByTaskAndStudentId(taskId, userId);

        LocalDateTime now = LocalDateTime.now();

        if (existRecord != null) {
            //记录已存在
            // 已经批改完毕，禁止提交
            if ("passed".equals(existRecord.getStatus())) {
                throw new StudentRepeatJoinCourseException("已提交，不可重复提交");
            }
            //更新状态为已提交，更新提交时间；分数、评语、批改时间保留原值
            studentSubmissionMapper.updateById(existRecord);
        } else {
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

        Double taskScore = studentCourseProgressMapper.getAvgTaskScore(userId);
        vo.setAverageAccuracy(taskScore / 100);

        return vo;
    }

    @Override
    public List<ErrorQuestionVO> listUserErrorQuestion(Long userId, Long courseId) {
        List<ErrorQuestionVO> errorQuestionVO = taskMapper.selectUserErrorQuestion(userId, courseId);

        if (CollectionUtils.isEmpty(errorQuestionVO)) {
            throw new ErrorQuestionNotFoundException("没有错题");
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
    public List<StudentAnswerVO> getStudentAnswers(Long studentId, Long submissionId) {
        List<StudentAnswer> list;
        if (submissionId != null) {
            list = studentAnswerMapper.selectBySubmissionId(submissionId);
        } else if (studentId != null) {
            list = studentAnswerMapper.selectByStudentId(studentId);
        } else {
            list = studentAnswerMapper.selectAll();
        }
        return list.stream().map(this::toAnswerVO).toList();
    }

    @Override
    public void updateStudentAnswer(StudentAnswerVO dto) {
        if (dto == null || dto.getId() == null) {
            throw new ParamInvalidException("答题记录ID不能为空");
        }
        StudentAnswer entity = studentAnswerMapper.selectById(dto.getId());
        if (entity == null) {
            throw new ParamInvalidException("答题记录不存在");
        }
        // 只更新可批改字段，createdAt 保持答题时间不变
        if (dto.getAnswer() != null) entity.setAnswer(dto.getAnswer().toString());
        if (dto.getScore() != null) entity.setScore(dto.getScore());
        if (dto.getComment() != null) entity.setComment(dto.getComment());
        if (dto.getTimeSpent() != null) entity.setTimeSpent(dto.getTimeSpent());
        if (dto.getAttemptCount() != null) entity.setAttemptCount(dto.getAttemptCount());
        if (dto.getHelpRequestCount() != null) entity.setHelpRequestCount(dto.getHelpRequestCount());
        if (dto.getErrorType() != null) entity.setErrorType(dto.getErrorType());
        studentAnswerMapper.updateById(entity);
    }

    @Override
    public List<StudentSubmission> getSubmissions(Long taskId) {
        if (taskId != null) {
            return studentSubmissionMapper.selectByTaskId(taskId);
        }
        return studentSubmissionMapper.selectAll();
    }

    /** 实体转VO：answer 字符串解析为 JSON 对象 */
    private StudentAnswerVO toAnswerVO(StudentAnswer e) {
        StudentAnswerVO vo = new StudentAnswerVO();
        BeanUtils.copyProperties(e, vo);
        if (e.getAnswer() != null && !e.getAnswer().isBlank()) {
            try {
                vo.setAnswer(objectMapper.readTree(e.getAnswer()));
            } catch (Exception ex) {
                log.warn("答题答案JSON解析失败, id={}", e.getId(), ex);
                vo.setAnswer(null);
            }
        }
        return vo;
    }

}
