package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.ai.model.QuestionGenerateModel;
import com.superyoudi.businessservice.mapper.QuestionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.QuestionPropertyMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.QuestionKnowledgeRel;
import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import com.superyoudi.businessservice.service.QuestionBaseService;
import com.superyoudi.common.dto.QuestionDTO;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public class QuestionBaseServiceImpl implements QuestionBaseService {
    private final QuestionMapper questionMapper;
    private final QuestionPropertyMapper questionPropertyMapper;
    private final QuestionGenerateModel questionGenerateModel;
    private final QuestionKnowledgeRelMapper questionKnowledgeRelMapper;

    public QuestionBaseServiceImpl(QuestionMapper questionMapper, QuestionPropertyMapper questionPropertyMapper, QuestionGenerateModel questionGenerateModel, QuestionKnowledgeRelMapper questionKnowledgeRelMapper) {
        this.questionMapper = questionMapper;
        this.questionPropertyMapper = questionPropertyMapper;
        this.questionGenerateModel = questionGenerateModel;
        this.questionKnowledgeRelMapper = questionKnowledgeRelMapper;
    }

    /**
     * 调用大模型生成题目
     *
     * @param assignerId   教师ID
     * @param courseId     课程ID
     * @param knowledge    知识点列表
     * @param questionType 问题类型
     * @param count        题目数量
     * @return 插入后的题目列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Question> generateQuestions(Long assignerId, Long courseId, List<String> knowledge, String questionType, int count) {
        QuestionGenerateModel.QuestionGenerateResult result = questionGenerateModel.call(knowledge, questionType, count);

        List<Question> questions = new ArrayList<>();

        for (QuestionGenerateModel.QuestionGenerateResult.QuestionData questionDatum : result.getQuestionData()) {
            Question question = new Question(null, courseId, assignerId, questionType,
                    questionDatum.getContent(), questionDatum.getAnswer().toString(), null, null);
            questionMapper.insert(question);
            questions.add(question);
        }

        return questions;
    }

    /**
     * 获取题库列表
     *
     * @param teacherId    教师ID
     * @param courseId     课程ID
     * @param questionType 问题类型。接受Question里定义的常量。传入null则查询所有题目。
     */
    @Override
    public List<Question> getQuestions(@NonNull Long teacherId, @NonNull Long courseId, @Nullable String questionType) {
        Assert.notNull(teacherId, "teacherId不能为null");
        Assert.notNull(courseId, "courseId不能为null");

        List<Question> questions;

        if (questionType == null) {
            questions = questionMapper.findByTeacherIdAndCourseId(teacherId, courseId);
        } else {
            questions = questionMapper.findByTeacherIdAndCourseIdAndType(teacherId, courseId, questionType);
        }

        return questions;

    }

    @Override
    public List<Question> getQuestionsByKnowledge(String knowledge) {
        return questionMapper.findQuestionsByKnowledge(knowledge);
    }

    /**
     * 添加题目与题目属性。
     * <p>
     * 若题目属性为null，则只添加题目。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addQuestion(@Nullable QuestionDTO dto) {
        Assert.notNull(dto, "question不能为null");
        Assert.notNull(dto.getTeacherId(), "teacherId不能为null");
        Assert.notNull(dto.getCourseId(), "courseId不能为null");
        Assert.hasText(dto.getContent(), "题干不能为空");

        // 添加题目
        Question question = Question.builder()
                .courseId(dto.getCourseId())
                .teacherId(dto.getTeacherId())
                .type(dto.getType())
                .content(dto.getContent())
                .answer(dto.getAnswer())
                .build();
        Long rows = questionMapper.insert(question);
        if (rows == null || rows == 0) {
            throw new RuntimeException("插入题目失败");
        }

        // 添加题目属性
        QuestionProperty property = QuestionProperty.builder()
                .questionId(question.getId())
                .analysis(dto.getAnalysis())
                .source(dto.getSource())
                .difficulty(dto.getDifficulty())
                .build();

        questionPropertyMapper.insert(property);

        // 添加题目与知识点的关系
        List<String> knowledgeList = dto.getKnowledgeList();
        for (int i = 0, knowledgeListSize = knowledgeList.size(); i < knowledgeListSize; i++) {
            String kp = knowledgeList.get(i);
            QuestionKnowledgeRel questionKnowledgeRel =
                    new QuestionKnowledgeRel(null, question.getId(), kp, i);
            questionKnowledgeRelMapper.insert(questionKnowledgeRel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Question question) {
        Long rows = questionMapper.update(question);
        if (rows == null || rows == 0) {
            throw new RuntimeException("插入题目失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteQuestion(Long questionId) {
        Long rows = questionMapper.deleteById(questionId);
        if (rows == null || rows == 0) {
            throw new RuntimeException("删除题目失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addQuestionProperty(QuestionProperty questionProperty) {
        // 参数校验
        Assert.notNull(questionProperty, "questionProperty不能为null");
        Assert.notNull(questionProperty.getQuestionId(), "questionId不能为null");
        Long rows = questionPropertyMapper.insert(questionProperty);
        if (rows == null || rows == 0) {
            throw new RuntimeException("插入题目属性失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateQuestionProperty(QuestionProperty questionProperty) {
        Assert.notNull(questionProperty, "questionProperty不能为null");
        Assert.notNull(questionProperty.getId(), "属性ID不能为null");
        Long rows = questionPropertyMapper.update(questionProperty);
        if (rows == null || rows == 0) {
            throw new RuntimeException("更新题目属性失败，ID: " + questionProperty.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionProperty(Long questionPropertyId) {
        Assert.notNull(questionPropertyId, "属性ID不能为null");
        Long rows = questionPropertyMapper.deleteById(questionPropertyId);
        if (rows == null || rows == 0) {
            throw new RuntimeException("删除题目属性失败，ID: " + questionPropertyId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionProperty getQuestionPropertyByQuestionId(Long questionId) {
        Assert.notNull(questionId, "questionId不能为null");
        return questionPropertyMapper.findByQuestionId(questionId);
    }
}
