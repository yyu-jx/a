package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.LearningPathService;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.UserMapper;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.entity.User;
import com.superyoudi.common.exception.LearningPathFinishedException;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiTutorServiceImpl 单元测试")
class AiTutorServiceImplTest {

    // 测试常量
    private static final Long STUDENT_ID = 1L;
    private static final Long COURSE_ID = 100L;
    private static final Long LEARNING_PATH_ID = 10L;
    private static final Long CURRENT_NODE_ID = 5L;
    private static final Long NEXT_NODE_ID = 6L;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private LearningPathService learningPathService;
    @Mock
    private KnowledgePointService knowledgePointService;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private AiTutorServiceImpl aiTutorService;

    @Nested
    @DisplayName("toNextLearningPathNode 方法测试")
    class ToNextLearningPathNodeTests {

        private LearningPath learningPath;

        @BeforeEach
        void setUp() {
            learningPath = new LearningPath();
            learningPath.setId(LEARNING_PATH_ID);
            learningPath.setStudentId(STUDENT_ID);
            learningPath.setCourseId(COURSE_ID);
            learningPath.setCurrentNodeId(CURRENT_NODE_ID);
            learningPath.setCurrentStage(LearningPath.STAGE_INTRODUCING);
        }

        @Test
        @DisplayName("正常推进到下一节点 - 应该更新状态并推进")
        void shouldAdvanceToNextNodeSuccessfully() throws LearningPathFinishedException {
            // Given
            when(learningPathService.getLearningPathById(LEARNING_PATH_ID)).thenReturn(learningPath);
            when(learningPathService.getNextLearningPathNode(LEARNING_PATH_ID, CURRENT_NODE_ID))
                    .thenReturn(NEXT_NODE_ID);

            // When
            aiTutorService.toNextLearningPathNode(LEARNING_PATH_ID);

            // Then
            verify(learningPathService).getLearningPathById(LEARNING_PATH_ID);
            verify(learningPathService).getNextLearningPathNode(LEARNING_PATH_ID, CURRENT_NODE_ID);

            // 验证当前节点被标记为已完成
            verify(learningPathService).updateLearningPathNodeStatus(
                    CURRENT_NODE_ID, LearningPathNode.NODE_STATUS_COMPLETED);

            // 验证下一节点被标记为进行中
            verify(learningPathService).updateLearningPathNodeStatus(
                    NEXT_NODE_ID, LearningPathNode.NODE_STATUS_PROCEEDING);

            // 验证学习路径被更新
            verify(learningPathService).updateLearningPath(learningPath);

            // 验证 learningPath 对象的状态更新
            assertThat(learningPath.getCurrentNodeId()).isEqualTo(NEXT_NODE_ID);
            assertThat(learningPath.getCurrentStage()).isEqualTo(LearningPath.STAGE_INTRODUCING);
        }

        @Test
        @DisplayName("学习路径已完成 - 下一节点ID等于当前节点ID时应抛出异常")
        void shouldThrowExceptionWhenLearningPathFinished() throws LearningPathFinishedException {
            // Given
            when(learningPathService.getLearningPathById(LEARNING_PATH_ID)).thenReturn(learningPath);
            when(learningPathService.getNextLearningPathNode(LEARNING_PATH_ID, CURRENT_NODE_ID))
                    .thenReturn(CURRENT_NODE_ID);

            // When & Then
            assertThatThrownBy(() -> aiTutorService.toNextLearningPathNode(LEARNING_PATH_ID))
                    .isInstanceOf(LearningPathFinishedException.class)
                    .hasMessage("学习路径已完成");

            // 验证当前节点仍被标记为已完成
            verify(learningPathService).updateLearningPathNodeStatus(
                    CURRENT_NODE_ID, LearningPathNode.NODE_STATUS_COMPLETED);

            // 验证下一节点没有被标记为进行中
            verify(learningPathService, never()).updateLearningPathNodeStatus(
                    eq(NEXT_NODE_ID), any());

            // 验证学习路径没有被更新
            verify(learningPathService, never()).updateLearningPath(any());
        }

        @Test
        @DisplayName("推进到最后一个有效节点 - 最后一个节点ID不等于当前节点ID")
        void shouldAdvanceToLastNodeSuccessfully() throws LearningPathFinishedException {
            // Given
            Long lastNodeId = 10L;
            when(learningPathService.getLearningPathById(LEARNING_PATH_ID)).thenReturn(learningPath);
            when(learningPathService.getNextLearningPathNode(LEARNING_PATH_ID, CURRENT_NODE_ID))
                    .thenReturn(lastNodeId);

            // When
            aiTutorService.toNextLearningPathNode(LEARNING_PATH_ID);

            // Then
            verify(learningPathService).updateLearningPathNodeStatus(
                    CURRENT_NODE_ID, LearningPathNode.NODE_STATUS_COMPLETED);
            verify(learningPathService).updateLearningPathNodeStatus(
                    lastNodeId, LearningPathNode.NODE_STATUS_PROCEEDING);
            verify(learningPathService).updateLearningPath(learningPath);

            assertThat(learningPath.getCurrentNodeId()).isEqualTo(lastNodeId);
            assertThat(learningPath.getCurrentStage()).isEqualTo(LearningPath.STAGE_INTRODUCING);
        }
    }

    @Nested
    @DisplayName("getLearningPathInfo 方法测试")
    class GetLearningPathInfoTests {

        private LearningPath learningPath;
        private LearningPathNode learningPathNode;
        private Course course;
        private KnowledgePointDTO knowledgePointDTO;

        @BeforeEach
        void setUp() {
            learningPath = new LearningPath();
            learningPath.setId(LEARNING_PATH_ID);
            learningPath.setCourseId(COURSE_ID);
            learningPath.setCurrentNodeId(CURRENT_NODE_ID);
            learningPath.setCurrentStage(LearningPath.STAGE_EXPLAINING);

            learningPathNode = new LearningPathNode();
            learningPathNode.setId(CURRENT_NODE_ID);

            course = new Course();
            course.setId(COURSE_ID);
            course.setName("Java 编程基础");

            knowledgePointDTO = new KnowledgePointDTO();
            knowledgePointDTO.setKnowledgeDescription("面向对象编程");
        }

        @Test
        @DisplayName("获取学习路径信息 - 返回完整的学习路径DTO")
        void shouldReturnLearningPathDTOWithAllFields() {
            // Given
            when(learningPathService.getLearningPathByStudentAndCourse(STUDENT_ID, COURSE_ID))
                    .thenReturn(learningPath);
            when(learningPathService.getLearningPathNodeById(CURRENT_NODE_ID))
                    .thenReturn(learningPathNode);
            when(courseMapper.selectById(COURSE_ID)).thenReturn(course);
            when(knowledgePointService.getKnowledgePoint(anyString())).thenReturn(knowledgePointDTO);

            // When
            LearningPathDTO result = aiTutorService.getLearningPathInfo(STUDENT_ID, COURSE_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo(COURSE_ID);
            assertThat(result.getCourseName()).isEqualTo("Java 编程基础");
            assertThat(result.getLearningPathId()).isEqualTo(LEARNING_PATH_ID);
            assertThat(result.getCurrentPathNodeId()).isEqualTo(CURRENT_NODE_ID);
            assertThat(result.getCurrentNodeName()).isEqualTo("面向对象编程");
            assertThat(result.getCurrentStage()).isEqualTo(LearningPath.STAGE_EXPLAINING);

            verify(learningPathService).getLearningPathByStudentAndCourse(STUDENT_ID, COURSE_ID);
            verify(learningPathService).getLearningPathNodeById(CURRENT_NODE_ID);
            verify(courseMapper).selectById(COURSE_ID);
            verify(knowledgePointService).getKnowledgePoint(anyString());
        }

        @Test
        @DisplayName("获取学习路径信息 - 知识点标题为空时的处理")
        void shouldHandleNullKnowledgeTitle() {
            // Given
            KnowledgePointDTO emptyKpDto = new KnowledgePointDTO();
            emptyKpDto.setKnowledgeDescription(null);

            when(learningPathService.getLearningPathByStudentAndCourse(STUDENT_ID, COURSE_ID))
                    .thenReturn(learningPath);
            when(learningPathService.getLearningPathNodeById(CURRENT_NODE_ID))
                    .thenReturn(learningPathNode);
            when(courseMapper.selectById(COURSE_ID)).thenReturn(course);
            when(knowledgePointService.getKnowledgePoint(anyString())).thenReturn(emptyKpDto);

            // When
            LearningPathDTO result = aiTutorService.getLearningPathInfo(STUDENT_ID, COURSE_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCurrentNodeName()).isNull();
        }

        @Test
        @DisplayName("获取学习路径信息 - 课程名称为空时的处理")
        void shouldHandleNullCourseName() {
            // Given
            Course courseWithNullName = new Course();
            courseWithNullName.setId(COURSE_ID);
            courseWithNullName.setName(null);

            when(learningPathService.getLearningPathByStudentAndCourse(STUDENT_ID, COURSE_ID))
                    .thenReturn(learningPath);
            when(learningPathService.getLearningPathNodeById(CURRENT_NODE_ID))
                    .thenReturn(learningPathNode);
            when(courseMapper.selectById(COURSE_ID)).thenReturn(courseWithNullName);
            when(knowledgePointService.getKnowledgePoint(anyString())).thenReturn(knowledgePointDTO);

            // When
            LearningPathDTO result = aiTutorService.getLearningPathInfo(STUDENT_ID, COURSE_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCourseName()).isNull();
        }
    }

    @Nested
    @DisplayName("getStudentInfo 方法测试")
    class GetStudentInfoTests {

        @Test
        @DisplayName("获取学生信息 - 返回正确的学生信息DTO")
        void shouldReturnStudentInfoDTO() {
            // Given
            User student = new User();
            student.setId(STUDENT_ID);
            student.setRealname("张三");

            when(userMapper.selectById(STUDENT_ID)).thenReturn(student);

            // When
            StudentCourseInfoDTO result = aiTutorService.getStudentInfo(STUDENT_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStudentId()).isEqualTo("1");
            assertThat(result.getStudentName()).isEqualTo("张三");

            verify(userMapper).selectById(STUDENT_ID);
        }

        @Test
        @DisplayName("获取学生信息 - 用户不存在时应该抛出NullPointerException")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userMapper.selectById(STUDENT_ID)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> aiTutorService.getStudentInfo(STUDENT_ID))
                    .isInstanceOf(NullPointerException.class);

            verify(userMapper).selectById(STUDENT_ID);
        }

        @Test
        @DisplayName("获取学生信息 - 真实姓名为空时的处理")
        void shouldHandleNullRealname() {
            // Given
            User student = new User();
            student.setId(STUDENT_ID);
            student.setRealname(null);

            when(userMapper.selectById(STUDENT_ID)).thenReturn(student);

            // When
            StudentCourseInfoDTO result = aiTutorService.getStudentInfo(STUDENT_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStudentId()).isEqualTo("1");
            assertThat(result.getStudentName()).isNull();
        }

        @Test
        @DisplayName("获取学生信息 - ID转换为字符串")
        void shouldConvertIdToString() {
            // Given
            Long longStudentId = 99999L;
            User student = new User();
            student.setId(longStudentId);
            student.setRealname("李四");

            when(userMapper.selectById(longStudentId)).thenReturn(student);

            // When
            StudentCourseInfoDTO result = aiTutorService.getStudentInfo(longStudentId);

            // Then
            assertThat(result.getStudentId()).isEqualTo("99999");
            assertThat(result.getStudentId()).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("formatLearningPath 方法测试")
    class FormatLearningPathTests {

        @Test
        @DisplayName("格式化学习路径 - 包含多个节点")
        void shouldFormatLearningPathWithMultipleNodes() {
            // Given
            LearningPathNode node1 = createLearningPathNode(1L, "100", "进行中");
            LearningPathNode node2 = createLearningPathNode(2L, "200", "已完成");
            LearningPathNode node3 = createLearningPathNode(3L, "300", "未开始");

            List<LearningPathNode> nodes = Arrays.asList(node1, node2, node3);

            KnowledgePointDTO kp1 = createKnowledgePointDTO("Java基础");
            KnowledgePointDTO kp2 = createKnowledgePointDTO("Spring框架");
            KnowledgePointDTO kp3 = createKnowledgePointDTO("数据库设计");

            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID)).thenReturn(nodes);
            when(knowledgePointService.getKnowledgePoint("100")).thenReturn(kp1);
            when(knowledgePointService.getKnowledgePoint("200")).thenReturn(kp2);
            when(knowledgePointService.getKnowledgePoint("300")).thenReturn(kp3);

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("学习路径 " + LEARNING_PATH_ID + "：");
            assertThat(result).contains("节点ID：1");
            assertThat(result).contains("知识点：Java基础");
            assertThat(result).contains("知识点ID：100");
            assertThat(result).contains("状态：进行中");
            assertThat(result).contains("节点ID：2");
            assertThat(result).contains("知识点：Spring框架");
            assertThat(result).contains("知识点ID：200");
            assertThat(result).contains("状态：已完成");
            assertThat(result).contains("节点ID：3");
            assertThat(result).contains("知识点：数据库设计");
            assertThat(result).contains("知识点ID：300");
            assertThat(result).contains("状态：未开始");

            verify(learningPathService).getLearningPathNodes(LEARNING_PATH_ID);
            verify(knowledgePointService, times(1)).getKnowledgePoint("100");
            verify(knowledgePointService, times(1)).getKnowledgePoint("200");
            verify(knowledgePointService, times(1)).getKnowledgePoint("300");
        }

        @Test
        @DisplayName("格式化学习路径 - 节点列表为null")
        void shouldReturnEmptyMessageWhenNodeListIsNull() {
            // Given
            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID)).thenReturn(null);

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            assertThat(result).isEqualTo("该学习路径暂无任何节点内容。");
            verify(knowledgePointService, never()).getKnowledgePoint(anyString());
        }

        @Test
        @DisplayName("格式化学习路径 - 节点列表为空")
        void shouldReturnEmptyMessageWhenNodeListIsEmpty() {
            // Given
            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID))
                    .thenReturn(Collections.emptyList());

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            assertThat(result).isEqualTo("该学习路径暂无任何节点内容。");
            verify(knowledgePointService, never()).getKnowledgePoint(anyString());
        }

        @Test
        @DisplayName("格式化学习路径 - 知识点DTO为null时显示未知知识点")
        void shouldHandleNullKnowledgePointDTO() {
            // Given
            LearningPathNode node = createLearningPathNode(1L, "100", "进行中");
            List<LearningPathNode> nodes = Collections.singletonList(node);

            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID)).thenReturn(nodes);
            when(knowledgePointService.getKnowledgePoint("100")).thenReturn(null);

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            assertThat(result).contains("知识点：未知知识点");
            assertThat(result).contains("知识点ID：100");
            assertThat(result).contains("状态：进行中");
        }

        @Test
        @DisplayName("格式化学习路径 - 知识点标题为null时显示未知知识点")
        void shouldDisplayUnknownWhenKnowledgeTitleIsNull() {
            // Given
            LearningPathNode node = createLearningPathNode(1L, "100", "进行中");
            List<LearningPathNode> nodes = Collections.singletonList(node);

            KnowledgePointDTO kpDto = new KnowledgePointDTO();
            kpDto.setKnowledgeDescription(null);

            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID)).thenReturn(nodes);
            when(knowledgePointService.getKnowledgePoint("100")).thenReturn(kpDto);

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            assertThat(result).contains("知识点：未知知识点");
        }

        @Test
        @DisplayName("格式化学习路径 - 单个节点的格式正确性")
        void shouldFormatSingleNodeCorrectly() {
            // Given
            LearningPathNode node = createLearningPathNode(5L, "500", "已完成");
            List<LearningPathNode> nodes = Collections.singletonList(node);

            KnowledgePointDTO kpDto = createKnowledgePointDTO("设计模式");

            when(learningPathService.getLearningPathNodes(LEARNING_PATH_ID)).thenReturn(nodes);
            when(knowledgePointService.getKnowledgePoint("500")).thenReturn(kpDto);

            // When
            String result = aiTutorService.formatLearningPath(LEARNING_PATH_ID);

            // Then
            String expectedNodeInfo = "节点ID：5，知识点：设计模式，知识点ID：500，状态：已完成";
            assertThat(result).contains(expectedNodeInfo);
            assertThat(result).startsWith("学习路径 " + LEARNING_PATH_ID + "：\n");
        }

        private LearningPathNode createLearningPathNode(Long id, String knowledgeId, String status) {
            LearningPathNode node = new LearningPathNode();
            node.setId(id);
            node.setStatus(status);
            return node;
        }

        private KnowledgePointDTO createKnowledgePointDTO(String title) {
            KnowledgePointDTO dto = new KnowledgePointDTO();
            dto.setKnowledgeDescription(title);
            return dto;
        }
    }
}