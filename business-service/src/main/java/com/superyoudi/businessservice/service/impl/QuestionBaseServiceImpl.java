package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.QuestionPropertyMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import com.superyoudi.businessservice.service.QuestionBaseService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public class QuestionBaseServiceImpl implements QuestionBaseService {
    private final QuestionMapper questionMapper;
    private final QuestionPropertyMapper questionPropertyMapper;

    public QuestionBaseServiceImpl(QuestionMapper questionMapper, QuestionPropertyMapper questionPropertyMapper) {
        this.questionMapper = questionMapper;
        this.questionPropertyMapper = questionPropertyMapper;
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

    /**
     * 添加题目与题目属性。
     * <p>
     * 若题目属性为null，则只添加题目。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestion(@NonNull Question question, @Nullable QuestionProperty questionProperty) {
        Assert.notNull(question, "question不能为null");
        Assert.notNull(question.getTeacherId(), "teacherId不能为null");
        Assert.notNull(question.getCourseId(), "courseId不能为null");
        Assert.hasText(question.getContent(), "题干不能为空");

        Long rows = questionMapper.insert(question);
        if (rows == null || rows == 0) {
            throw new RuntimeException("插入题目失败");
        }
        if (questionProperty != null) {
            questionProperty.setQuestionId(question.getId());
            questionPropertyMapper.insert(questionProperty);
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
    public QuestionProperty getQuestionPropertyByQuestionId(Long questionId) {
        Assert.notNull(questionId, "questionId不能为null");
        return questionPropertyMapper.findByQuestionId(questionId);
    }
}
