package com.superyoudi.businessservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.service.TaskAiService;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.dto.TaskQuestionDTO;
import com.superyoudi.businessservice.pojo.entity.*;
import com.superyoudi.businessservice.pojo.vo.StudentTaskOverviewVO;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.businessservice.utils.AnswerValidator;
import com.superyoudi.common.dto.ErrorQuestionVO;
import com.superyoudi.common.exception.BusinessException;
import com.superyoudi.common.exception.ErrorQuestionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final TaskQuestionRelMapper taskQuestionRelMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
     * @return
     */
    @Override
    public Long addOrUpdateStudentSubmission(StudentSubmission submission) {
        // 参数校验
        Assert.notNull(submission, "提交记录不能为空");

        if (submission.getId() != null) {
            studentSubmissionMapper.update(submission);
        } else {
            studentSubmissionMapper.insert(submission);
        }

        if (submission.getStatus().equals("submitted")) {
            executeSubmissionCorrection(submission);
        }

        return submission.getId();
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
        if ("teacher".equals(role)) {
            // 教师：查自己布置的全部任务
            return taskMapper.selectTeacherTaskPage(pageable, userId, courseId, status);
        } else {
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
    public List<TaskQuestionDTO> getTaskQuestionList(Long taskId) {
        //  校验参数
        if (taskId == null) {
            throw new BusinessException("任务ID不能为空");
        }

        //  查询任务基本信息
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        //  查询任务关联的所有题目（按题号排序）
        return questionMapper.selectByTaskId(taskId);
    }

    @Override
    public List<StudentAnswer> getStudentAnswers(Long studentId, Long submissionId) {
        return studentAnswerMapper.selectByStudentIdAndSubmissionId(studentId, submissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addOrUpdateStudentAnswer(StudentAnswer answer) {
        Assert.notNull(answer, "答题记录ID不能为空");

        // id为null，新增
        if (answer.getId() == null) {
            Long rows = studentAnswerMapper.insert(answer);

            if (rows == null || rows == 0)
                throw new RuntimeException("StudentAnswer插入失败");
            return answer.getId();
        }
        studentAnswerMapper.update(answer);
        return answer.getId();
    }

    @Override
    public List<StudentSubmission> getSubmissions(Long taskId) {
        return studentSubmissionMapper.selectByTaskId(taskId);
    }

    /**
     * 执行任务批改
     */
    @Override
    public void executeSubmissionCorrection(StudentSubmission submission) {
        Assert.notNull(submission, "提交记录不能为空");
        Assert.notNull(submission.getTaskId(), "任务ID不能为空");

        // 获取任务信息
        Task task = taskMapper.selectById(submission.getTaskId());
        if (task == null)
            throw new BusinessException("任务不存在");

        if (task.getAfterSubmitStrategy().equals(Task.SUBMIT_STRATEGY_MANUAL))
            return;

        String afterSubmitStrategy = task.getAfterSubmitStrategy();

        List<StudentAnswer> studentAnswers = getStudentAnswers(submission.getStudentId(), submission.getId());

        Map<StudentAnswer, Question> studentAnswerQuestionMap = new HashMap<>();
        for (StudentAnswer studentAnswer : studentAnswers) {
            Question answerQuestion = questionMapper.findById(studentAnswer.getQuestionId());
            if (answerQuestion == null)
                throw new BusinessException("题目不存在");

            studentAnswerQuestionMap.put(studentAnswer, answerQuestion);
        }


        switch (afterSubmitStrategy) {
            case Task.SUBMIT_STRATEGY_AUTO_OBJECTIVE -> {
                // 自动批改客观题
                correctObjectiveQuestions(task.getId(), studentAnswerQuestionMap);
            }
            case Task.SUBMIT_STRATEGY_AUTO_ALL -> {
                // 批改全部题目
            }
        }
    }

    /**
     * 批改客观题
     *
     * @param taskId            任务ID
     * @param answerQuestionMap 学生答案与题目
     */
    public void correctObjectiveQuestions(Long taskId, Map<StudentAnswer, Question> answerQuestionMap) {
        Map<StudentAnswer, Question> objectiveQuestionsMap = answerQuestionMap.entrySet().stream()
                // 过滤出客观题
                .filter(entry ->
                        entry.getValue().getType().equals("choice") ||
                                entry.getValue().getType().equals("true_false")
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<StudentAnswer, Question> answerQuestionEntry : objectiveQuestionsMap.entrySet()) {
            StudentAnswer answer = answerQuestionEntry.getKey();
            Question question = answerQuestionEntry.getValue();

            Object correctAnswer = convertQuestionAnswerJson(question.getType(), question.getAnswer());
            Object studentAnswer = convertStudentAnswerJson(question.getType(), answer.getAnswer());

            int questionScore = taskQuestionRelMapper.findByTaskIdAndQuestionId(taskId, question.getId()).getScore();

            // 选择题
            if (correctAnswer instanceof Question.ChoiceAnswer correct &&
                    studentAnswer instanceof StudentAnswer.ChoiceAnswer student) {
                int correctAnswerCount = AnswerValidator.validateChoiceAnswer(correct, student);

                // 计算学生得分
                int finalScore = AnswerValidator.calcStudentAnswerScore(questionScore, correct, correctAnswerCount);

                // 更新学生答案记录
                answer.setScore(finalScore);
            }
            // 填空题
            else if (correctAnswer instanceof Question.TrueFalseAnswer correct &&
                    studentAnswer instanceof StudentAnswer.TrueFalseAnswer student) {
                int score = AnswerValidator.validateTrueFalseAnswer(correct, student) ? 1 : 0;
                int finalScore = score * questionScore;
                answer.setScore(finalScore);
            }
            studentAnswerMapper.update(answer);
        }
    }

    public void correctSubjectiveQuestions(Map<StudentAnswer, Question> answerQuestionMap) {

    }

    public Object convertQuestionAnswerJson(String type, String json) {

        try {
            return switch (type) {
                case "choice" -> objectMapper.readValue(json, Question.ChoiceAnswer.class);
                case "fill" -> objectMapper.readValue(json, Question.FillAnswer.class);
                case "subjective" -> objectMapper.readValue(json, Question.SubjectiveAnswer.class);
                case "true_false" -> objectMapper.readValue(json, Question.TrueFalseAnswer.class);
                default -> null;
            };

        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }

    }

    public Object convertStudentAnswerJson(String type, String json) {
        try {
            return switch (type) {
                case "choice" -> objectMapper.readValue(json, StudentAnswer.ChoiceAnswer.class);
                case "fill" -> objectMapper.readValue(json, StudentAnswer.FillAnswer.class);
                case "subjective" -> objectMapper.readValue(json, StudentAnswer.SubjectiveAnswer.class);
                case "true_false" -> objectMapper.readValue(json, StudentAnswer.TrueFalseAnswer.class);
                default -> null;
            };
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }
}
