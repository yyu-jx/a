package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public interface QuestionBaseService {

    List<Question> getQuestions(Long teacherId, Long courseId, String questionType);

    List<Question> getQuestionsByKnowledge(String knowledge);

    void addQuestion(Question question, QuestionProperty questionProperty);

    void updateQuestion(Question question);

    void deleteQuestion(Long questionId);

    void addQuestionProperty(QuestionProperty questionProperty);

    void updateQuestionProperty(QuestionProperty questionProperty);

    void deleteQuestionProperty(Long questionPropertyId);

    QuestionProperty getQuestionPropertyByQuestionId(Long questionId);
}
