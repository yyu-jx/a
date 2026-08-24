package com.superyoudi.businessservice.service.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.superyoudi.aiservice.ai.model.QuestionGenerateModel;
import com.superyoudi.businessservice.mapper.QuestionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.QuestionMapper;
import com.superyoudi.businessservice.mapper.QuestionPropertyMapper;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.QuestionKnowledgeRel;
import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import com.superyoudi.common.dto.QuestionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Sijayyx
 */

@ExtendWith(MockitoExtension.class)
class QuestionBaseServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private QuestionPropertyMapper questionPropertyMapper;

    @Mock
    private QuestionGenerateModel questionGenerateModel;

    @Mock
    private QuestionKnowledgeRelMapper questionKnowledgeRelMapper;

    @InjectMocks
    private QuestionBaseServiceImpl questionBaseService;

    private Question testQuestion;
    private QuestionProperty testProperty;
    private QuestionDTO testDTO;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testQuestion = Question.builder()
                .id(1L)
                .courseId(100L)
                .teacherId(200L)
                .type("choice")
                .content("测试题目内容")
                .answer("{\"select\":\"A\"}")
                .build();

        testProperty = QuestionProperty.builder()
                .id(1L)
                .questionId(1L)
                .analysis("测试解析")
                .source("manual")
                .difficulty("easy")
                .build();

        testDTO = QuestionDTO.builder()
                .courseId(100L)
                .teacherId(200L)
                .type("choice")
                .content("测试题目内容")
                .answer("{\"select\":\"A\"}")
                .analysis("测试解析")
                .source("manual")
                .difficulty("easy")
                .knowledgeList(Arrays.asList("知识点1", "知识点2"))
                .build();
    }

    @Nested
    @DisplayName("generateQuestions 方法测试")
    class GenerateQuestionsTest {

        @Test
        @DisplayName("成功生成题目")
        void shouldGenerateQuestionsSuccessfully() {
            // Arrange
            Long assignerId = 200L;
            Long courseId = 100L;
            List<String> knowledge = Arrays.asList("知识点1", "知识点2");
            String questionType = "choice";
            int count = 2;

            QuestionGenerateModel.QuestionGenerateResult result =
                    new QuestionGenerateModel.QuestionGenerateResult();
            QuestionGenerateModel.QuestionGenerateResult.QuestionData data1 =
                    new QuestionGenerateModel.QuestionGenerateResult.QuestionData();
            data1.setContent("题目1");
            data1.setAnswer(JsonNodeFactory.instance.objectNode().put("select", "A"));

            QuestionGenerateModel.QuestionGenerateResult.QuestionData data2 =
                    new QuestionGenerateModel.QuestionGenerateResult.QuestionData();
            data2.setContent("题目2");
            data2.setAnswer(JsonNodeFactory.instance.objectNode().put("select", "B"));

            result.setQuestionData(Arrays.asList(data1, data2));

            when(questionGenerateModel.call(knowledge, questionType, count)).thenReturn(result);
            when(questionMapper.insert(any(Question.class))).thenReturn(1L);

            // Act
            List<Question> questions = questionBaseService.generateQuestions(
                    assignerId, courseId, knowledge, questionType, count);

            // Assert
            assertNotNull(questions);
            assertEquals(2, questions.size());
            verify(questionGenerateModel, times(1)).call(knowledge, questionType, count);
            verify(questionMapper, times(2)).insert(any(Question.class));

            // 验证生成的题目属性
            Question generatedQuestion = questions.get(0);
            assertEquals(courseId, generatedQuestion.getCourseId());
            assertEquals(assignerId, generatedQuestion.getTeacherId());
            assertEquals(questionType, generatedQuestion.getType());

            // 验证答案被正确转换为字符串
            assertEquals("{\"select\":\"A\"}", generatedQuestion.getAnswer());
        }

        @Test
        @DisplayName("大模型返回空列表")
        void shouldReturnEmptyListWhenModelReturnsEmpty() {
            // Arrange
            Long assignerId = 200L;
            Long courseId = 100L;
            List<String> knowledge = Arrays.asList("知识点1");
            String questionType = "choice";
            int count = 1;

            QuestionGenerateModel.QuestionGenerateResult result =
                    new QuestionGenerateModel.QuestionGenerateResult();
            result.setQuestionData(Collections.emptyList());

            when(questionGenerateModel.call(knowledge, questionType, count)).thenReturn(result);

            // Act
            List<Question> questions = questionBaseService.generateQuestions(
                    assignerId, courseId, knowledge, questionType, count);

            // Assert
            assertNotNull(questions);
            assertTrue(questions.isEmpty());
            verify(questionMapper, never()).insert(any(Question.class));
        }

        @Test
        @DisplayName("大模型调用异常")
        void shouldThrowExceptionWhenModelFails() {
            // Arrange
            Long assignerId = 200L;
            Long courseId = 100L;
            List<String> knowledge = Arrays.asList("知识点1");
            String questionType = "choice";
            int count = 1;

            when(questionGenerateModel.call(anyList(), anyString(), anyInt()))
                    .thenThrow(new RuntimeException("模型调用失败"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.generateQuestions(assignerId, courseId, knowledge, questionType, count);
            });
            verify(questionMapper, never()).insert(any(Question.class));
        }
    }

    @Nested
    @DisplayName("getQuestions 方法测试")
    class GetQuestionsTest {

        @Test
        @DisplayName("查询所有题目（不指定类型）")
        void shouldGetAllQuestionsWhenTypeIsNull() {
            // Arrange
            Long teacherId = 200L;
            Long courseId = 100L;
            List<Question> expectedQuestions = Arrays.asList(testQuestion);

            when(questionMapper.findByTeacherIdAndCourseId(teacherId, courseId))
                    .thenReturn(expectedQuestions);

            // Act
            List<Question> questions = questionBaseService.getQuestions(teacherId, courseId, null);

            // Assert
            assertNotNull(questions);
            assertEquals(1, questions.size());
            verify(questionMapper, times(1)).findByTeacherIdAndCourseId(teacherId, courseId);
        }

        @Test
        @DisplayName("查询指定类型的题目")
        void shouldGetQuestionsByType() {
            // Arrange
            Long teacherId = 200L;
            Long courseId = 100L;
            String questionType = "choice";
            List<Question> expectedQuestions = Arrays.asList(testQuestion);

            when(questionMapper.findByTeacherIdAndCourseIdAndType(teacherId, courseId, questionType))
                    .thenReturn(expectedQuestions);

            // Act
            List<Question> questions = questionBaseService.getQuestions(teacherId, courseId, questionType);

            // Assert
            assertNotNull(questions);
            assertEquals(1, questions.size());
            verify(questionMapper, times(1))
                    .findByTeacherIdAndCourseIdAndType(teacherId, courseId, questionType);
        }

        @Test
        @DisplayName("teacherId为null时抛出异常")
        void shouldThrowExceptionWhenTeacherIdIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.getQuestions(null, 100L, null);
            });
        }

        @Test
        @DisplayName("courseId为null时抛出异常")
        void shouldThrowExceptionWhenCourseIdIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.getQuestions(200L, null, null);
            });
        }
    }

    @Nested
    @DisplayName("getQuestionsByKnowledge 方法测试")
    class GetQuestionsByKnowledgeTest {

        @Test
        @DisplayName("根据知识点查询题目")
        void shouldGetQuestionsByKnowledge() {
            // Arrange
            String knowledge = "知识点1";
            List<Question> expectedQuestions = Arrays.asList(testQuestion);

            when(questionMapper.findQuestionsByKnowledge(knowledge))
                    .thenReturn(expectedQuestions);

            // Act
            List<Question> questions = questionBaseService.getQuestionsByKnowledge(knowledge);

            // Assert
            assertNotNull(questions);
            assertEquals(1, questions.size());
            verify(questionMapper, times(1)).findQuestionsByKnowledge(knowledge);
        }

        @Test
        @DisplayName("根据知识点查询无结果")
        void shouldReturnEmptyListWhenNoQuestionsFound() {
            // Arrange
            String knowledge = "不存在的知识点";

            when(questionMapper.findQuestionsByKnowledge(knowledge))
                    .thenReturn(Collections.emptyList());

            // Act
            List<Question> questions = questionBaseService.getQuestionsByKnowledge(knowledge);

            // Assert
            assertNotNull(questions);
            assertTrue(questions.isEmpty());
        }
    }

    @Nested
    @DisplayName("addQuestion 方法测试")
    class AddQuestionTest {

        @Test
        @DisplayName("成功添加题目及属性")
        void shouldAddQuestionWithPropertySuccessfully() {
            // Arrange
            when(questionMapper.insert(any(Question.class))).thenReturn(1L);
            when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(1L);
            when(questionKnowledgeRelMapper.insert(any(QuestionKnowledgeRel.class))).thenReturn(1L);

            // Act
            questionBaseService.addQuestion(testDTO);

            // Assert
            ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
            verify(questionMapper, times(1)).insert(questionCaptor.capture());

            Question capturedQuestion = questionCaptor.getValue();
            assertEquals(testDTO.getCourseId(), capturedQuestion.getCourseId());
            assertEquals(testDTO.getTeacherId(), capturedQuestion.getTeacherId());
            assertEquals(testDTO.getType(), capturedQuestion.getType());
            assertEquals(testDTO.getContent(), capturedQuestion.getContent());
            assertEquals(testDTO.getAnswer(), capturedQuestion.getAnswer());

            // 验证属性插入
            ArgumentCaptor<QuestionProperty> propertyCaptor = ArgumentCaptor.forClass(QuestionProperty.class);
            verify(questionPropertyMapper, times(1)).insert(propertyCaptor.capture());

            QuestionProperty capturedProperty = propertyCaptor.getValue();
            assertEquals(testDTO.getAnalysis(), capturedProperty.getAnalysis());
            assertEquals(testDTO.getSource(), capturedProperty.getSource());
            assertEquals(testDTO.getDifficulty(), capturedProperty.getDifficulty());

            // 验证知识点关系插入
            verify(questionKnowledgeRelMapper, times(2)).insert(any(QuestionKnowledgeRel.class));
        }

        @Test
        @DisplayName("添加题目时DTO为null抛出异常")
        void shouldThrowExceptionWhenDTOIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.addQuestion(null);
            });
            verify(questionMapper, never()).insert(any(Question.class));
        }

        @Test
        @DisplayName("添加题目时teacherId为null抛出异常")
        void shouldThrowExceptionWhenTeacherIdIsNull() {
            // Arrange
            testDTO.setTeacherId(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.addQuestion(testDTO);
            });
            verify(questionMapper, never()).insert(any(Question.class));
        }

        @Test
        @DisplayName("添加题目时content为空抛出异常")
        void shouldThrowExceptionWhenContentIsEmpty() {
            // Arrange
            testDTO.setContent("");

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.addQuestion(testDTO);
            });
            verify(questionMapper, never()).insert(any(Question.class));
        }

        @Test
        @DisplayName("插入题目失败抛出异常")
        void shouldThrowExceptionWhenInsertFails() {
            // Arrange
            when(questionMapper.insert(any(Question.class))).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.addQuestion(testDTO);
            });
            verify(questionPropertyMapper, never()).insert(any(QuestionProperty.class));
            verify(questionKnowledgeRelMapper, never()).insert(any(QuestionKnowledgeRel.class));
        }

        @Test
        @DisplayName("添加题目时knowledgeList为空列表")
        void shouldAddQuestionWhenKnowledgeListIsEmpty() {
            // Arrange
            testDTO.setKnowledgeList(Collections.emptyList());
            when(questionMapper.insert(any(Question.class))).thenReturn(1L);
            when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(1L);

            // Act
            questionBaseService.addQuestion(testDTO);

            // Assert
            verify(questionMapper, times(1)).insert(any(Question.class));
            verify(questionPropertyMapper, times(1)).insert(any(QuestionProperty.class));
            verify(questionKnowledgeRelMapper, never()).insert(any(QuestionKnowledgeRel.class));
        }
    }

    @Nested
    @DisplayName("updateQuestion 方法测试")
    class UpdateQuestionTest {

        @Test
        @DisplayName("成功更新题目")
        void shouldUpdateQuestionSuccessfully() {
            // Arrange
            when(questionMapper.update(any(Question.class))).thenReturn(1L);

            // Act
            questionBaseService.updateQuestion(testQuestion);

            // Assert
            verify(questionMapper, times(1)).update(testQuestion);
        }

        @Test
        @DisplayName("更新题目失败抛出异常")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            when(questionMapper.update(any(Question.class))).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.updateQuestion(testQuestion);
            });
        }

        @Test
        @DisplayName("更新不存在的题目")
        void shouldThrowExceptionWhenUpdateNonExistentQuestion() {
            // Arrange
            when(questionMapper.update(any(Question.class))).thenReturn(null);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.updateQuestion(testQuestion);
            });
        }
    }

    @Nested
    @DisplayName("deleteQuestion 方法测试")
    class DeleteQuestionTest {

        @Test
        @DisplayName("成功删除题目")
        void shouldDeleteQuestionSuccessfully() {
            // Arrange
            Long questionId = 1L;
            when(questionMapper.deleteById(questionId)).thenReturn(1L);

            // Act
            questionBaseService.deleteQuestion(questionId);

            // Assert
            verify(questionMapper, times(1)).deleteById(questionId);
        }

        @Test
        @DisplayName("删除题目失败抛出异常")
        void shouldThrowExceptionWhenDeleteFails() {
            // Arrange
            Long questionId = 1L;
            when(questionMapper.deleteById(questionId)).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.deleteQuestion(questionId);
            });
        }

        @Test
        @DisplayName("删除不存在的题目")
        void shouldThrowExceptionWhenDeleteNonExistentQuestion() {
            // Arrange
            Long questionId = 999L;
            when(questionMapper.deleteById(questionId)).thenReturn(null);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.deleteQuestion(questionId);
            });
        }
    }

    @Nested
    @DisplayName("addQuestionProperty 方法测试")
    class AddQuestionPropertyTest {

        @Test
        @DisplayName("成功添加题目属性")
        void shouldAddQuestionPropertySuccessfully() {
            // Arrange
            when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(1L);

            // Act
            questionBaseService.addQuestionProperty(testProperty);

            // Assert
            verify(questionPropertyMapper, times(1)).insert(testProperty);
        }

        @Test
        @DisplayName("添加题目属性为null抛出异常")
        void shouldThrowExceptionWhenPropertyIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.addQuestionProperty(null);
            });
            verify(questionPropertyMapper, never()).insert(any(QuestionProperty.class));
        }

        @Test
        @DisplayName("添加题目属性questionId为null抛出异常")
        void shouldThrowExceptionWhenQuestionIdIsNull() {
            // Arrange
            testProperty.setQuestionId(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.addQuestionProperty(testProperty);
            });
            verify(questionPropertyMapper, never()).insert(any(QuestionProperty.class));
        }

        @Test
        @DisplayName("插入题目属性失败抛出异常")
        void shouldThrowExceptionWhenInsertFails() {
            // Arrange
            when(questionPropertyMapper.insert(any(QuestionProperty.class))).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.addQuestionProperty(testProperty);
            });
        }
    }

    @Nested
    @DisplayName("updateQuestionProperty 方法测试")
    class UpdateQuestionPropertyTest {

        @Test
        @DisplayName("成功更新题目属性")
        void shouldUpdateQuestionPropertySuccessfully() {
            // Arrange
            when(questionPropertyMapper.update(any(QuestionProperty.class))).thenReturn(1L);

            // Act
            questionBaseService.updateQuestionProperty(testProperty);

            // Assert
            verify(questionPropertyMapper, times(1)).update(testProperty);
        }

        @Test
        @DisplayName("更新题目属性为null抛出异常")
        void shouldThrowExceptionWhenPropertyIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.updateQuestionProperty(null);
            });
            verify(questionPropertyMapper, never()).update(any(QuestionProperty.class));
        }

        @Test
        @DisplayName("更新题目属性ID为null抛出异常")
        void shouldThrowExceptionWhenPropertyIdIsNull() {
            // Arrange
            testProperty.setId(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.updateQuestionProperty(testProperty);
            });
            verify(questionPropertyMapper, never()).update(any(QuestionProperty.class));
        }

        @Test
        @DisplayName("更新题目属性失败抛出异常")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            when(questionPropertyMapper.update(any(QuestionProperty.class))).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.updateQuestionProperty(testProperty);
            });
        }
    }

    @Nested
    @DisplayName("deleteQuestionProperty 方法测试")
    class DeleteQuestionPropertyTest {

        @Test
        @DisplayName("成功删除题目属性")
        void shouldDeleteQuestionPropertySuccessfully() {
            // Arrange
            Long propertyId = 1L;
            when(questionPropertyMapper.deleteById(propertyId)).thenReturn(1L);

            // Act
            questionBaseService.deleteQuestionProperty(propertyId);

            // Assert
            verify(questionPropertyMapper, times(1)).deleteById(propertyId);
        }

        @Test
        @DisplayName("删除题目属性ID为null抛出异常")
        void shouldThrowExceptionWhenPropertyIdIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.deleteQuestionProperty(null);
            });
            verify(questionPropertyMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("删除题目属性失败抛出异常")
        void shouldThrowExceptionWhenDeleteFails() {
            // Arrange
            Long propertyId = 1L;
            when(questionPropertyMapper.deleteById(propertyId)).thenReturn(0L);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                questionBaseService.deleteQuestionProperty(propertyId);
            });
        }
    }

    @Nested
    @DisplayName("getQuestionPropertyByQuestionId 方法测试")
    class GetQuestionPropertyByQuestionIdTest {

        @Test
        @DisplayName("成功获取题目属性")
        void shouldGetQuestionPropertySuccessfully() {
            // Arrange
            Long questionId = 1L;
            when(questionPropertyMapper.findByQuestionId(questionId)).thenReturn(testProperty);

            // Act
            QuestionProperty property = questionBaseService.getQuestionPropertyByQuestionId(questionId);

            // Assert
            assertNotNull(property);
            assertEquals(testProperty, property);
            verify(questionPropertyMapper, times(1)).findByQuestionId(questionId);
        }

        @Test
        @DisplayName("获取题目属性questionId为null抛出异常")
        void shouldThrowExceptionWhenQuestionIdIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                questionBaseService.getQuestionPropertyByQuestionId(null);
            });
            verify(questionPropertyMapper, never()).findByQuestionId(anyLong());
        }

        @Test
        @DisplayName("获取不存在的题目属性返回null")
        void shouldReturnNullWhenPropertyNotFound() {
            // Arrange
            Long questionId = 999L;
            when(questionPropertyMapper.findByQuestionId(questionId)).thenReturn(null);

            // Act
            QuestionProperty property = questionBaseService.getQuestionPropertyByQuestionId(questionId);

            // Assert
            assertNull(property);
        }
    }
}