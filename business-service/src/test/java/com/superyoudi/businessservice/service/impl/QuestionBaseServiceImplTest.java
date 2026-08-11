package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.QuestionPropertyMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import com.superyoudi.common.constants.QuestionConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Sijayyx
 */
@ExtendWith(MockitoExtension.class)
class QuestionBaseServiceImplTest {

    private final Long TEST_TEACHER_ID = 1L;
    private final Long TEST_COURSE_ID = 100L;
    private final Long TEST_QUESTION_ID = 1000L;
    private final Long TEST_PROPERTY_ID = 2000L;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private QuestionPropertyMapper questionPropertyMapper;
    @InjectMocks
    private QuestionBaseServiceImpl questionBaseService;
    private Question testQuestion;
    private QuestionProperty testProperty;

    @BeforeEach
    void setUp() {
        testQuestion = Question.builder()
                .id(TEST_QUESTION_ID)
                .teacherId(TEST_TEACHER_ID)
                .courseId(TEST_COURSE_ID)
                .type(QuestionConstants.TYPE_CHOICE)
                .content("测试题目内容")
                .answer("{\"A\":\"选项A\",\"B\":\"选项B\"}")
                .build();

        testProperty = QuestionProperty.builder()
                .id(TEST_PROPERTY_ID)
                .questionId(TEST_QUESTION_ID)
                .analysis("本题考查了XXX知识点")
                .source(QuestionConstants.SOURCE_MANUAL)
                .difficulty(QuestionConstants.DIFFICULTY_MEDIUM)
                .build();
    }

    // ========== getQuestions 测试 ==========
    @Test
    void getQuestions_WhenQuestionTypeIsNull_ShouldFindAllByTeacherAndCourse() {
        // Arrange
        List<Question> expectedQuestions = Arrays.asList(testQuestion);
        when(questionMapper.findByTeacherIdAndCourseId(TEST_TEACHER_ID, TEST_COURSE_ID))
                .thenReturn(expectedQuestions);

        // Act
        List<Question> result = questionBaseService.getQuestions(TEST_TEACHER_ID, TEST_COURSE_ID, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion, result.get(0));
        verify(questionMapper, times(1))
                .findByTeacherIdAndCourseId(TEST_TEACHER_ID, TEST_COURSE_ID);
        verify(questionMapper, never())
                .findByTeacherIdAndCourseIdAndType(anyLong(), anyLong(), anyString());
    }

    @Test
    void getQuestions_WhenQuestionTypeIsNotNull_ShouldFindByTeacherAndCourseAndType() {
        // Arrange
        String questionType = QuestionConstants.TYPE_CHOICE;
        List<Question> expectedQuestions = Arrays.asList(testQuestion);
        when(questionMapper.findByTeacherIdAndCourseIdAndType(TEST_TEACHER_ID, TEST_COURSE_ID, questionType))
                .thenReturn(expectedQuestions);

        // Act
        List<Question> result = questionBaseService.getQuestions(TEST_TEACHER_ID, TEST_COURSE_ID, questionType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion, result.get(0));
        verify(questionMapper, never())
                .findByTeacherIdAndCourseId(TEST_TEACHER_ID, TEST_COURSE_ID);
        verify(questionMapper, times(1))
                .findByTeacherIdAndCourseIdAndType(TEST_TEACHER_ID, TEST_COURSE_ID, questionType);
    }

    @Test
    void getQuestions_WhenNoQuestionsFound_ShouldReturnEmptyList() {
        // Arrange
        when(questionMapper.findByTeacherIdAndCourseId(TEST_TEACHER_ID, TEST_COURSE_ID))
                .thenReturn(List.of());

        // Act
        List<Question> result = questionBaseService.getQuestions(TEST_TEACHER_ID, TEST_COURSE_ID, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(questionMapper, times(1))
                .findByTeacherIdAndCourseId(TEST_TEACHER_ID, TEST_COURSE_ID);
    }

    @Test
    void getQuestions_WhenTeacherIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionBaseService.getQuestions(null, TEST_COURSE_ID, null));
    }

    @Test
    void getQuestions_WhenCourseIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionBaseService.getQuestions(TEST_TEACHER_ID, null, null));
    }

    // ========== addQuestion 测试 ==========
    @Test
    void addQuestion_WhenQuestionPropertyIsNull_ShouldInsertQuestionOnly() {
        // Arrange
        when(questionMapper.insert(testQuestion)).thenReturn(TEST_QUESTION_ID);

        // Act
        questionBaseService.addQuestion(testQuestion, null);

        // Assert
        verify(questionMapper, times(1)).insert(testQuestion);
        verify(questionPropertyMapper, never()).insert(any(QuestionProperty.class));
    }

    @Test
    void addQuestion_WhenQuestionPropertyIsNotNull_ShouldInsertBoth() {
        // Arrange
        when(questionMapper.insert(testQuestion)).thenReturn(TEST_QUESTION_ID);
        when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(TEST_PROPERTY_ID);

        // Act
        questionBaseService.addQuestion(testQuestion, testProperty);

        // Assert
        verify(questionMapper, times(1)).insert(testQuestion);

        ArgumentCaptor<QuestionProperty> propertyCaptor = ArgumentCaptor.forClass(QuestionProperty.class);
        verify(questionPropertyMapper, times(1)).insert(propertyCaptor.capture());

        QuestionProperty capturedProperty = propertyCaptor.getValue();
        assertEquals(TEST_QUESTION_ID, capturedProperty.getQuestionId());
        assertEquals(testProperty.getAnalysis(), capturedProperty.getAnalysis());
        assertEquals(testProperty.getSource(), capturedProperty.getSource());
        assertEquals(testProperty.getDifficulty(), capturedProperty.getDifficulty());
    }

    @Test
    void addQuestion_WhenQuestionIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionBaseService.addQuestion(null, testProperty));
    }

    @Test
    void addQuestion_WhenInsertFails_ShouldThrowException() {
        // Arrange
        when(questionMapper.insert(testQuestion)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.addQuestion(testQuestion, null));
    }

    // ========== updateQuestion 测试 ==========
    @Test
    void updateQuestion_ShouldCallMapperUpdate() {
        // Arrange
        when(questionMapper.update(testQuestion)).thenReturn(1L);

        // Act
        questionBaseService.updateQuestion(testQuestion);

        // Assert
        verify(questionMapper, times(1)).update(testQuestion);
    }

    @Test
    void updateQuestion_WhenUpdateFails_ShouldThrowException() {
        // Arrange
        when(questionMapper.update(testQuestion)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.updateQuestion(testQuestion));
    }

    @Test
    void updateQuestion_WhenQuestionIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.updateQuestion(null));
    }

    @Test
    void updateQuestion_ShouldUpdateAllFields() {
        // Arrange
        Question updatedQuestion = Question.builder()
                .id(TEST_QUESTION_ID)
                .teacherId(TEST_TEACHER_ID)
                .courseId(TEST_COURSE_ID)
                .type(QuestionConstants.TYPE_SUBJECTIVE)
                .content("更新后的题目内容")
                .answer("更新后的答案")
                .build();

        when(questionMapper.update(updatedQuestion)).thenReturn(1L);

        // Act
        questionBaseService.updateQuestion(updatedQuestion);

        // Assert
        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper, times(1)).update(questionCaptor.capture());

        Question captured = questionCaptor.getValue();
        assertEquals(TEST_QUESTION_ID, captured.getId());
        assertEquals(QuestionConstants.TYPE_SUBJECTIVE, captured.getType());
        assertEquals("更新后的题目内容", captured.getContent());
    }

    // ========== deleteQuestion 测试 ==========
    @Test
    void deleteQuestion_ShouldCallMapperDelete() {
        // Arrange
        when(questionMapper.deleteById(TEST_QUESTION_ID)).thenReturn(1L);

        // Act
        questionBaseService.deleteQuestion(TEST_QUESTION_ID);

        // Assert
        verify(questionMapper, times(1)).deleteById(TEST_QUESTION_ID);
    }

    @Test
    void deleteQuestion_WhenDeleteFails_ShouldThrowException() {
        // Arrange
        when(questionMapper.deleteById(TEST_QUESTION_ID)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.deleteQuestion(TEST_QUESTION_ID));
    }

    @Test
    void deleteQuestion_WhenIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.deleteQuestion(null));
    }

    // ========== addQuestionProperty 测试 ==========
    @Test
    void addQuestionProperty_ShouldCallMapperInsert() {
        // Arrange
        when(questionPropertyMapper.insert(testProperty)).thenReturn(TEST_PROPERTY_ID);

        // Act
        questionBaseService.addQuestionProperty(testProperty);

        // Assert
        verify(questionPropertyMapper, times(1)).insert(testProperty);
    }

    @Test
    void addQuestionProperty_WhenInsertFails_ShouldThrowException() {
        // Arrange
        when(questionPropertyMapper.insert(testProperty)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.addQuestionProperty(testProperty));
    }

    @Test
    void addQuestionProperty_WhenPropertyIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.addQuestionProperty(null));
    }

    // ========== updateQuestionProperty 测试 ==========
    @Test
    void updateQuestionProperty_ShouldCallMapperUpdate() {
        // Arrange
        when(questionPropertyMapper.update(testProperty)).thenReturn(1L);

        // Act
        questionBaseService.updateQuestionProperty(testProperty);

        // Assert
        verify(questionPropertyMapper, times(1)).update(testProperty);
    }

    @Test
    void updateQuestionProperty_WhenUpdateFails_ShouldThrowException() {
        // Arrange
        when(questionPropertyMapper.update(testProperty)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.updateQuestionProperty(testProperty));
    }

    @Test
    void updateQuestionProperty_ShouldUpdateAllFields() {
        // Arrange
        QuestionProperty updatedProperty = QuestionProperty.builder()
                .id(TEST_PROPERTY_ID)
                .questionId(TEST_QUESTION_ID)
                .analysis("更新后的解析")
                .source(QuestionConstants.SOURCE_AI)
                .difficulty(QuestionConstants.DIFFICULTY_HARD)
                .build();

        when(questionPropertyMapper.update(updatedProperty)).thenReturn(1L);

        // Act
        questionBaseService.updateQuestionProperty(updatedProperty);

        // Assert
        ArgumentCaptor<QuestionProperty> propertyCaptor = ArgumentCaptor.forClass(QuestionProperty.class);
        verify(questionPropertyMapper, times(1)).update(propertyCaptor.capture());

        QuestionProperty captured = propertyCaptor.getValue();
        assertEquals(TEST_PROPERTY_ID, captured.getId());
        assertEquals("更新后的解析", captured.getAnalysis());
        assertEquals(QuestionConstants.SOURCE_AI, captured.getSource());
        assertEquals(QuestionConstants.DIFFICULTY_HARD, captured.getDifficulty());
    }

    // ========== deleteQuestionProperty 测试 ==========
    @Test
    void deleteQuestionProperty_ShouldCallMapperDelete() {
        // Arrange
        when(questionPropertyMapper.deleteById(TEST_PROPERTY_ID)).thenReturn(1L);

        // Act
        questionBaseService.deleteQuestionProperty(TEST_PROPERTY_ID);

        // Assert
        verify(questionPropertyMapper, times(1)).deleteById(TEST_PROPERTY_ID);
    }

    @Test
    void deleteQuestionProperty_WhenDeleteFails_ShouldThrowException() {
        // Arrange
        when(questionPropertyMapper.deleteById(TEST_PROPERTY_ID)).thenReturn(0L);

        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.deleteQuestionProperty(TEST_PROPERTY_ID));
    }

    @Test
    void deleteQuestionProperty_WhenIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class,
                () -> questionBaseService.deleteQuestionProperty(null));
    }

    // ========== getQuestionPropertyByQuestionId 测试 ==========
    @Test
    void getQuestionPropertyByQuestionId_ShouldCallMapperFind() {
        // Arrange
        when(questionPropertyMapper.findByQuestionId(TEST_QUESTION_ID)).thenReturn(testProperty);

        // Act
        QuestionProperty result = questionBaseService.getQuestionPropertyByQuestionId(TEST_QUESTION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(testProperty, result);
        verify(questionPropertyMapper, times(1)).findByQuestionId(TEST_QUESTION_ID);
    }

    @Test
    void getQuestionPropertyByQuestionId_WhenPropertyNotFound_ShouldReturnNull() {
        // Arrange
        when(questionPropertyMapper.findByQuestionId(TEST_QUESTION_ID)).thenReturn(null);

        // Act
        QuestionProperty result = questionBaseService.getQuestionPropertyByQuestionId(TEST_QUESTION_ID);

        // Assert
        assertNull(result);
        verify(questionPropertyMapper, times(1)).findByQuestionId(TEST_QUESTION_ID);
    }

    @Test
    void getQuestionPropertyByQuestionId_WhenIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionBaseService.getQuestionPropertyByQuestionId(null));
    }

    // ========== 集成场景测试 ==========
    @Test
    void addQuestion_WithProperty_ShouldSetQuestionIdCorrectly() {
        // Arrange
        Question newQuestion = Question.builder()
                .teacherId(TEST_TEACHER_ID)
                .courseId(TEST_COURSE_ID)
                .type(QuestionConstants.TYPE_FILL)
                .content("新的填空题")
                .build();

        QuestionProperty newProperty = QuestionProperty.builder()
                .analysis("本题考查填空")
                .source(QuestionConstants.SOURCE_MANUAL)
                .difficulty(QuestionConstants.DIFFICULTY_EASY)
                .build();

        // 模拟 insert 返回影响行数，并设置 ID
        when(questionMapper.insert(newQuestion)).thenAnswer(invocation -> {
            newQuestion.setId(999L);
            return 1L;
        });

        // 模拟属性插入返回影响行数
        when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(1L);

        // Act
        questionBaseService.addQuestion(newQuestion, newProperty);

        // Assert
        // 1. 验证 question 的 ID 被设置
        assertNotNull(newQuestion.getId());
        assertEquals(999L, newQuestion.getId());

        // 2. 验证属性插入被调用
        verify(questionPropertyMapper, times(1)).insert(any(QuestionProperty.class));

        // 3. 捕获参数并验证
        ArgumentCaptor<QuestionProperty> propertyCaptor = ArgumentCaptor.forClass(QuestionProperty.class);
        verify(questionPropertyMapper).insert(propertyCaptor.capture());

        QuestionProperty capturedProperty = propertyCaptor.getValue();
        assertNotNull(capturedProperty); // 这里不应该为 null
        assertEquals(999L, capturedProperty.getQuestionId());
        assertEquals("本题考查填空", capturedProperty.getAnalysis());
        assertEquals(QuestionConstants.SOURCE_MANUAL, capturedProperty.getSource());
        assertEquals(QuestionConstants.DIFFICULTY_EASY, capturedProperty.getDifficulty());
    }

    @Test
    void deleteQuestion_ShouldDeleteWithoutCheckingProperty() {
        // Arrange
        when(questionMapper.deleteById(TEST_QUESTION_ID)).thenReturn(1L);

        // Act
        questionBaseService.deleteQuestion(TEST_QUESTION_ID);

        // Assert
        verify(questionMapper, times(1)).deleteById(TEST_QUESTION_ID);
        // 由于级联删除，不需要显式调用删除属性
        verify(questionPropertyMapper, never()).deleteByQuestionId(anyLong());
    }

    @Test
    void getQuestions_WithDifferentQuestionTypes_ShouldCallCorrectMapperMethod() {
        // Test with choice type
        when(questionMapper.findByTeacherIdAndCourseIdAndType(
                TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_CHOICE))
                .thenReturn(List.of());

        questionBaseService.getQuestions(TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_CHOICE);

        verify(questionMapper, times(1))
                .findByTeacherIdAndCourseIdAndType(
                        TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_CHOICE);

        // Test with fill type
        when(questionMapper.findByTeacherIdAndCourseIdAndType(
                TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_FILL))
                .thenReturn(List.of());

        questionBaseService.getQuestions(TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_FILL);

        verify(questionMapper, times(1))
                .findByTeacherIdAndCourseIdAndType(
                        TEST_TEACHER_ID, TEST_COURSE_ID, QuestionConstants.TYPE_FILL);
    }
}