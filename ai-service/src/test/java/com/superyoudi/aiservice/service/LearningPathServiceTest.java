package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {

    @Mock
    private LearningPathMapper learningPathMapper;

    @Mock
    private LearningPathNodeMapper learningPathNodeMapper;

    @InjectMocks
    private LearningPathService learningPathService;

    private LearningPath mockLearningPath;
    private LearningPathNode mockLearningPathNode;
    private List<LearningPathNode> mockNodeList;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        mockLearningPath = new LearningPath();
        mockLearningPath.setId(1L);
        mockLearningPath.setStudentId(100L);
        mockLearningPath.setCourseId(200L);
        mockLearningPath.setCurrentNodeId(10L);
        mockLearningPath.setCurrentStage(LearningPath.STAGE_EXPLAINING);
        mockLearningPath.setAdditionalInfo("Test additional info");
        mockLearningPath.setCreatedAt(now);
        mockLearningPath.setUpdatedAt(now);

        mockLearningPathNode = new LearningPathNode();
        mockLearningPathNode.setId(10L);
        mockLearningPathNode.setPathId(1L);
        mockLearningPathNode.setKnowledgeId("knowledge-001");
        mockLearningPathNode.setStatus(LearningPathNode.NODE_STATUS_PROCEEDING);
        mockLearningPathNode.setRelatedTaskId(50L);
        mockLearningPathNode.setCreatedAt(now);
        mockLearningPathNode.setUpdatedAt(now);

        mockNodeList = Arrays.asList(
                mockLearningPathNode,
                createNode(11L, 1L, "knowledge-002", LearningPathNode.NODE_STATUS_UNTESTED, 51L),
                createNode(12L, 1L, "knowledge-003", LearningPathNode.NODE_STATUS_COMPLETED, 52L)
        );
    }

    private LearningPathNode createNode(Long id, Long pathId, String knowledgeId, String status, Long relatedTaskId) {
        LearningPathNode node = new LearningPathNode();
        node.setId(id);
        node.setPathId(pathId);
        node.setKnowledgeId(knowledgeId);
        node.setStatus(status);
        node.setRelatedTaskId(relatedTaskId);
        node.setCreatedAt(now);
        node.setUpdatedAt(now);
        return node;
    }

    @Test
    void getLearningPathNodes_ByStudentAndCourse_ShouldReturnNodeList() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(mockLearningPath);
        when(learningPathNodeMapper.selectByPathId(mockLearningPath.getId()))
                .thenReturn(mockNodeList);

        // When
        List<LearningPathNode> result = learningPathService.getLearningPathNodes(studentId, courseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockNodeList);
        verify(learningPathMapper).selectByStudentAndCourse(studentId, courseId);
        verify(learningPathNodeMapper).selectByPathId(mockLearningPath.getId());
    }

    @Test
    void getLearningPathNodes_ByStudentAndCourse_WhenLearningPathNotFound_ShouldThrowNullPointerException() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> learningPathService.getLearningPathNodes(studentId, courseId))
                .isInstanceOf(NullPointerException.class);
        verify(learningPathMapper).selectByStudentAndCourse(studentId, courseId);
        verify(learningPathNodeMapper, never()).selectByPathId(anyLong());
    }

    @Test
    void getLearningPathNodes_ByPathId_ShouldReturnNodeList() {
        // Given
        Long pathId = 1L;
        when(learningPathNodeMapper.selectByPathId(pathId))
                .thenReturn(mockNodeList);

        // When
        List<LearningPathNode> result = learningPathService.getLearningPathNodes(pathId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockNodeList);
        verify(learningPathNodeMapper).selectByPathId(pathId);
    }

    @Test
    void getLearningPathNodes_ByPathId_WhenNoNodesExist_ShouldReturnEmptyList() {
        // Given
        Long pathId = 999L;
        when(learningPathNodeMapper.selectByPathId(pathId))
                .thenReturn(Arrays.asList());

        // When
        List<LearningPathNode> result = learningPathService.getLearningPathNodes(pathId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(learningPathNodeMapper).selectByPathId(pathId);
    }

    @Test
    void getNextLearningPathNode_ShouldReturnNextNodeId() {
        // Given
        Long pathId = 1L;
        Long currentNodeId = 10L;
        Long expectedNextNodeId = 11L;
        when(learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId))
                .thenReturn(expectedNextNodeId);

        // When
        Long result = learningPathService.getNextLearningPathNode(pathId, currentNodeId);

        // Then
        assertThat(result).isEqualTo(expectedNextNodeId);
        verify(learningPathNodeMapper).selectNextNodeId(pathId, currentNodeId);
    }

    @Test
    void getNextLearningPathNode_WhenNoNextNode_ShouldReturnNull() {
        // Given
        Long pathId = 1L;
        Long currentNodeId = 12L;
        when(learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId))
                .thenReturn(null);

        // When
        Long result = learningPathService.getNextLearningPathNode(pathId, currentNodeId);

        // Then
        assertThat(result).isNull();
        verify(learningPathNodeMapper).selectNextNodeId(pathId, currentNodeId);
    }

    @Test
    void getLearningPathById_ShouldReturnLearningPath() {
        // Given
        Long learningPathId = 1L;
        when(learningPathMapper.selectById(learningPathId))
                .thenReturn(mockLearningPath);

        // When
        LearningPath result = learningPathService.getLearningPathById(learningPathId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentId()).isEqualTo(100L);
        assertThat(result.getCourseId()).isEqualTo(200L);
        assertThat(result.getCurrentStage()).isEqualTo(LearningPath.STAGE_EXPLAINING);
        verify(learningPathMapper).selectById(learningPathId);
    }

    @Test
    void getLearningPathById_WhenNotFound_ShouldReturnNull() {
        // Given
        Long learningPathId = 999L;
        when(learningPathMapper.selectById(learningPathId))
                .thenReturn(null);

        // When
        LearningPath result = learningPathService.getLearningPathById(learningPathId);

        // Then
        assertThat(result).isNull();
        verify(learningPathMapper).selectById(learningPathId);
    }

    @Test
    void getLearningPathByStudentAndCourse_ShouldReturnLearningPath() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(mockLearningPath);

        // When
        LearningPath result = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentId()).isEqualTo(100L);
        assertThat(result.getCourseId()).isEqualTo(200L);
        verify(learningPathMapper).selectByStudentAndCourse(studentId, courseId);
    }

    @Test
    void getLearningPathByStudentAndCourse_WhenNotFound_ShouldReturnNull() {
        // Given
        Long studentId = 999L;
        Long courseId = 999L;
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(null);

        // When
        LearningPath result = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);

        // Then
        assertThat(result).isNull();
        verify(learningPathMapper).selectByStudentAndCourse(studentId, courseId);
    }

    @Test
    void getLearningPathNodeById_ShouldReturnLearningPathNode() {
        // Given
        Long nodeId = 10L;
        when(learningPathNodeMapper.selectById(nodeId))
                .thenReturn(mockLearningPathNode);

        // When
        LearningPathNode result = learningPathService.getLearningPathNodeById(nodeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getPathId()).isEqualTo(1L);
        assertThat(result.getKnowledgeId()).isEqualTo("knowledge-001");
        assertThat(result.getStatus()).isEqualTo(LearningPathNode.NODE_STATUS_PROCEEDING);
        verify(learningPathNodeMapper).selectById(nodeId);
    }

    @Test
    void getLearningPathNodeById_WhenNotFound_ShouldReturnNull() {
        // Given
        Long nodeId = 999L;
        when(learningPathNodeMapper.selectById(nodeId))
                .thenReturn(null);

        // When
        LearningPathNode result = learningPathService.getLearningPathNodeById(nodeId);

        // Then
        assertThat(result).isNull();
        verify(learningPathNodeMapper).selectById(nodeId);
    }

    @Test
    void updateLearningPathNodeStatus_ShouldUpdateStatus() {
        // Given
        Long nodeId = 10L;
        String newStatus = LearningPathNode.NODE_STATUS_COMPLETED;

        // When
        learningPathService.updateLearningPathNodeStatus(nodeId, newStatus);

        // Then
        verify(learningPathNodeMapper).updateStatus(nodeId, newStatus);
    }

    @Test
    void updateLearningPathNodeStatus_WithNullNodeId_ShouldStillCallMapper() {
        // Given
        Long nodeId = null;
        String newStatus = LearningPathNode.NODE_STATUS_PROCEEDING;

        // When
        learningPathService.updateLearningPathNodeStatus(nodeId, newStatus);

        // Then
        verify(learningPathNodeMapper).updateStatus(nodeId, newStatus);
    }

    @Test
    void updateLearningPath_ShouldUpdateLearningPath() {
        // Given
        LearningPath learningPathToUpdate = new LearningPath();
        learningPathToUpdate.setId(1L);
        learningPathToUpdate.setCurrentNodeId(12L);
        learningPathToUpdate.setCurrentStage(LearningPath.STAGE_TESTING);
        learningPathToUpdate.setAdditionalInfo("Updated info");

        // When
        learningPathService.updateLearningPath(learningPathToUpdate);

        // Then
        verify(learningPathMapper).update(learningPathToUpdate);
    }

    @Test
    void updateLearningPath_WithNullParameter_ShouldStillCallMapper() {
        // Given
        LearningPath learningPathToUpdate = null;

        // When
        learningPathService.updateLearningPath(learningPathToUpdate);

        // Then
        verify(learningPathMapper).update(learningPathToUpdate);
    }

    @Test
    void getLearningPathNodes_ByStudentAndCourse_ShouldVerifyMapperCalls() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(mockLearningPath);
        when(learningPathNodeMapper.selectByPathId(mockLearningPath.getId()))
                .thenReturn(mockNodeList);

        // When
        learningPathService.getLearningPathNodes(studentId, courseId);

        // Then
        verify(learningPathMapper, times(1))
                .selectByStudentAndCourse(studentId, courseId);
        verify(learningPathNodeMapper, times(1))
                .selectByPathId(mockLearningPath.getId());
        verifyNoMoreInteractions(learningPathMapper, learningPathNodeMapper);
    }

    @Test
    void getLearningPathNodes_ByPathId_ShouldVerifyMapperCalls() {
        // Given
        Long pathId = 1L;
        when(learningPathNodeMapper.selectByPathId(pathId))
                .thenReturn(mockNodeList);

        // When
        learningPathService.getLearningPathNodes(pathId);

        // Then
        verify(learningPathNodeMapper, times(1))
                .selectByPathId(pathId);
        verifyNoMoreInteractions(learningPathNodeMapper);
        verifyNoInteractions(learningPathMapper);
    }

    @Test
    void getNextLearningPathNode_ShouldVerifyMapperCalls() {
        // Given
        Long pathId = 1L;
        Long currentNodeId = 10L;
        when(learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId))
                .thenReturn(11L);

        // When
        learningPathService.getNextLearningPathNode(pathId, currentNodeId);

        // Then
        verify(learningPathNodeMapper, times(1))
                .selectNextNodeId(pathId, currentNodeId);
        verifyNoMoreInteractions(learningPathNodeMapper);
        verifyNoInteractions(learningPathMapper);
    }

    @Test
    void getLearningPathById_ShouldVerifyMapperCall() {
        // Given
        Long learningPathId = 1L;
        when(learningPathMapper.selectById(learningPathId))
                .thenReturn(mockLearningPath);

        // When
        learningPathService.getLearningPathById(learningPathId);

        // Then
        verify(learningPathMapper, times(1))
                .selectById(learningPathId);
        verifyNoMoreInteractions(learningPathMapper);
        verifyNoInteractions(learningPathNodeMapper);
    }

    @Test
    void updateLearningPathNodeStatus_WithVariousStatuses_ShouldCallMapper() {
        // Given
        Long nodeId = 10L;
        String[] statuses = {
                LearningPathNode.NODE_STATUS_UNTESTED,
                LearningPathNode.NODE_STATUS_PROCEEDING,
                LearningPathNode.NODE_STATUS_COMPLETED
        };

        // When & Then
        for (String status : statuses) {
            learningPathService.updateLearningPathNodeStatus(nodeId, status);
            verify(learningPathNodeMapper, times(1))
                    .updateStatus(nodeId, status);
        }
        verify(learningPathNodeMapper, times(3))
                .updateStatus(anyLong(), anyString());
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // Given
        LearningPathMapper mapper = mock(LearningPathMapper.class);
        LearningPathNodeMapper nodeMapper = mock(LearningPathNodeMapper.class);

        // When
        LearningPathService service = new LearningPathService(mapper, nodeMapper);

        // Then
        assertThat(service).isNotNull();
        // Verify the service can be used
        when(mapper.selectById(anyLong())).thenReturn(mockLearningPath);
        LearningPath result = service.getLearningPathById(1L);
        assertThat(result).isEqualTo(mockLearningPath);
    }
}
