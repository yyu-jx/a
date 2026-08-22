package com.superyoudi.businessservice;

import com.superyoudi.aiservice.ai.model.LearningPathGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.businessservice.mapper.SectionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.StudentKnowledgeMasteryMapper;
import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import com.superyoudi.businessservice.service.impl.LearningPathServiceImpl;
import com.superyoudi.common.dto.StudentKnowledgeMastery;
import com.superyoudi.common.exception.AllCourseKnowledgeMasteredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.gt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceImplTest {

    @Mock
    private LearningPathMapper learningPathMapper;

    @Mock
    private LearningPathNodeMapper learningPathNodeMapper;

    @Mock
    private SectionKnowledgeRelMapper sectionKnowledgeRelMapper;

    @Mock
    private StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;

    @Mock
    private LearningPathGenerateModel learningPathGenerateModel;

    @InjectMocks
    private LearningPathServiceImpl learningPathService;

    private Long studentId;
    private Long courseId;
    private List<SectionKnowledgeRel> knowledgeRels;
    private StudentKnowledgeMastery mastery;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        courseId = 100L;

        // 准备测试数据
        knowledgeRels = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            SectionKnowledgeRel rel = new SectionKnowledgeRel(
                    (long) i, courseId, (long) (i * 10), "kp_" + i, LocalDateTime.now()
            );
            knowledgeRels.add(rel);
        }

        mastery = new StudentKnowledgeMastery();
        mastery.setId(1L);
        mastery.setStudentId(studentId);
        mastery.setCourseId(courseId);
        mastery.setKnowledgeId("kp_1");
        mastery.setKnowledgeName("Java基础");
        mastery.setMasterRate(0.5);
    }

    @Test
    void testCreateLearningPath_Success() {
        // Given
        // 只 mock 实际会被查询的知识点（前35个）
        List<SectionKnowledgeRel> queryResult = knowledgeRels.subList(0, 35);
        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), eq(0L), eq(35)))
                .thenReturn(queryResult);

        // 只对实际返回的知识点设置 mock
        for (int i = 0; i < queryResult.size(); i++) {
            String knowledgeId = "kp_" + i;
            StudentKnowledgeMastery mastery = new StudentKnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setCourseId(courseId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setKnowledgeName("知识点" + i);

            // 设置不同的掌握度，确保有足够的未掌握知识点
            if (i < 3) {
                mastery.setMasterRate(0.9); // 已掌握，会被过滤
            } else {
                mastery.setMasterRate(0.3); // 未掌握，会被加入候选
            }

            when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(eq(studentId), eq(knowledgeId)))
                    .thenReturn(mastery);
        }

        // Mock AI生成的学习路径JSON
        String aiResponse = "{\"studentId\":1,\"courseId\":100,\"additionalInfo\":\"测试学习路径\"," +
                "\"pathNodeDataList\":[" +
                "{\"nodeName\":\"节点1\",\"knowledgeIds\":[\"kp_3\",\"kp_4\"]}," +
                "{\"nodeName\":\"节点2\",\"knowledgeIds\":[\"kp_5\",\"kp_6\"]}," +
                "{\"nodeName\":\"节点3\",\"knowledgeIds\":[\"kp_7\",\"kp_8\"]}]}";

        when(learningPathGenerateModel.call(eq(studentId), eq(courseId), anyString()))
                .thenReturn(aiResponse);

        // Mock旧的学习路径
        LearningPath oldPath = new LearningPath(1L, studentId, courseId, null,
                LearningPath.STAGE_INTRODUCING, null, null, null);
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(oldPath);

        // When
        learningPathService.createLearningPath(studentId, courseId);

        // Then
        // 验证删除了旧路径
        verify(learningPathNodeMapper).deleteAllByPathId(1L);
        verify(learningPathMapper).deleteById(1L);

        // 验证保存了新路径
        ArgumentCaptor<LearningPath> pathCaptor = ArgumentCaptor.forClass(LearningPath.class);
        verify(learningPathMapper).insert(pathCaptor.capture());
        LearningPath newPath = pathCaptor.getValue();
        assertEquals(studentId, newPath.getStudentId());
        assertEquals(courseId, newPath.getCourseId());
        assertEquals(LearningPath.STAGE_INTRODUCING, newPath.getCurrentStage());
        assertEquals("测试学习路径", newPath.getAdditionalInfo());

        // 验证保存了3个节点
        verify(learningPathNodeMapper, times(3)).insert(any(LearningPathNode.class));
    }

    @Test
    void testCreateLearningPath_AllMastered_ThrowsException() {
        // Given - 所有知识点都已掌握
        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), eq(0L), eq(35)))
                .thenReturn(knowledgeRels.subList(0, 35));

        StudentKnowledgeMastery highMastery = new StudentKnowledgeMastery();
        highMastery.setStudentId(studentId);
        highMastery.setCourseId(courseId);
        highMastery.setMasterRate(0.9);

        for (SectionKnowledgeRel rel : knowledgeRels.subList(0, 35)) {
            when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(
                    eq(studentId), eq(rel.getNeo4jKpId())))
                    .thenReturn(highMastery);
        }

        // When & Then
        assertThrows(AllCourseKnowledgeMasteredException.class, () -> {
            learningPathService.createLearningPath(studentId, courseId);
        });

        verify(learningPathGenerateModel, never()).call(anyLong(), anyLong(), anyString());
    }

    @Test
    void testCreateLearningPath_AIResponseNull() {
        // Given
        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), eq(0L), eq(35)))
                .thenReturn(knowledgeRels.subList(0, 35));

        when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(eq(studentId), anyString()))
                .thenReturn(null); // 无记录，视为未掌握

        when(learningPathGenerateModel.call(eq(studentId), eq(courseId), anyString()))
                .thenReturn("{}"); // AI返回空JSON

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            learningPathService.createLearningPath(studentId, courseId);
        });
    }

    @Test
    void testCreateLearningPath_RetryMechanism() {
        // Given - 模拟需要重试的情况
        // 第一次查询只有少量未掌握知识点
        List<SectionKnowledgeRel> firstBatch = knowledgeRels.subList(0, 35);
        List<SectionKnowledgeRel> secondBatch = knowledgeRels.subList(35, Math.min(70, knowledgeRels.size()));

        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), eq(0L), eq(35)))
                .thenReturn(firstBatch);
        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), gt(0L), eq(35)))
                .thenReturn(secondBatch);

        // 第一批大部分已掌握，只有5个未掌握
        for (int i = 0; i < 35; i++) {
            StudentKnowledgeMastery mastery = new StudentKnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setCourseId(courseId);
            mastery.setKnowledgeId("kp_" + i);
            mastery.setKnowledgeName("知识点" + i);
            mastery.setMasterRate(i < 5 ? 0.3 : 0.9); // 只有前5个未掌握
            when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(
                    eq(studentId), eq("kp_" + i))).thenReturn(mastery);
        }

        // 第二批有更多未掌握的
        for (int i = 35; i < Math.min(70, knowledgeRels.size()); i++) {
            StudentKnowledgeMastery mastery = new StudentKnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setCourseId(courseId);
            mastery.setKnowledgeId("kp_" + i);
            mastery.setKnowledgeName("知识点" + i);
            mastery.setMasterRate(0.3); // 都未掌握
            when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(
                    eq(studentId), eq("kp_" + i))).thenReturn(mastery);
        }

        // Mock AI响应
        String aiResponse = "{\"studentId\":1,\"courseId\":100,\"additionalInfo\":\"重试路径\"," +
                "\"pathNodeDataList\":[{\"nodeName\":\"节点1\",\"knowledgeIds\":[\"kp_0\",\"kp_1\"]}]}";
        when(learningPathGenerateModel.call(eq(studentId), eq(courseId), anyString()))
                .thenReturn(aiResponse);

        LearningPath oldPath = new LearningPath(2L, studentId, courseId, null,
                LearningPath.STAGE_INTRODUCING, null, null, null);
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(oldPath);

        // When
        learningPathService.createLearningPath(studentId, courseId);

        // Then - 验证至少调用了两次查询（重试）
        verify(sectionKnowledgeRelMapper, atLeast(2))
                .selectBehindStartByCourseDataId(eq(courseId), anyLong(), eq(35));
    }

    @Test
    void testGetLearningPathNodes_ByStudentAndCourse() {
        // Given
        LearningPath path = new LearningPath(1L, studentId, courseId, null,
                LearningPath.STAGE_INTRODUCING, null, null, null);
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId)).thenReturn(path);

        List<LearningPathNode> expectedNodes = Arrays.asList(
                new LearningPathNode(1L, 1L, "节点1", "[\"kp_1\"]",
                        LearningPathNode.NODE_STATUS_UNTESTED, null, null, null)
        );
        when(learningPathNodeMapper.selectByPathId(1L)).thenReturn(expectedNodes);

        // When
        List<LearningPathNode> result = learningPathService.getLearningPathNodes(studentId, courseId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("节点1", result.get(0).getNodeName());
    }

    @Test
    void testGetLearningPathNodes_ByPathId() {
        // Given
        Long pathId = 1L;
        List<LearningPathNode> expectedNodes = Arrays.asList(
                new LearningPathNode(1L, pathId, "节点1", "[\"kp_1\"]",
                        LearningPathNode.NODE_STATUS_UNTESTED, null, null, null),
                new LearningPathNode(2L, pathId, "节点2", "[\"kp_2\"]",
                        LearningPathNode.NODE_STATUS_UNTESTED, null, null, null)
        );
        when(learningPathNodeMapper.selectByPathId(pathId)).thenReturn(expectedNodes);

        // When
        List<LearningPathNode> result = learningPathService.getLearningPathNodes(pathId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetNextLearningPathNode() {
        // Given
        Long pathId = 1L;
        Long currentNodeId = 1L;
        Long expectedNextNodeId = 2L;
        when(learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId))
                .thenReturn(expectedNextNodeId);

        // When
        Long nextNodeId = learningPathService.getNextLearningPathNode(pathId, currentNodeId);

        // Then
        assertEquals(expectedNextNodeId, nextNodeId);
    }

    @Test
    void testGetLearningPathById() {
        // Given
        Long pathId = 1L;
        LearningPath expectedPath = new LearningPath(pathId, studentId, courseId, null,
                LearningPath.STAGE_INTRODUCING, null, null, null);
        when(learningPathMapper.selectById(pathId)).thenReturn(expectedPath);

        // When
        LearningPath result = learningPathService.getLearningPathById(pathId);

        // Then
        assertNotNull(result);
        assertEquals(pathId, result.getId());
        assertEquals(studentId, result.getStudentId());
    }

    @Test
    void testGetLearningPathByStudentAndCourse() {
        // Given
        LearningPath expectedPath = new LearningPath(1L, studentId, courseId, null,
                LearningPath.STAGE_EXPLAINING, "附加信息", null, null);
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(expectedPath);

        // When
        LearningPath result = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(LearningPath.STAGE_EXPLAINING, result.getCurrentStage());
    }

    @Test
    void testGetLearningPathNodeById() {
        // Given
        Long nodeId = 1L;
        LearningPathNode expectedNode = new LearningPathNode(nodeId, 1L, "节点1", "[\"kp_1\"]",
                LearningPathNode.NODE_STATUS_PROCEEDING, 100L, null, null);
        when(learningPathNodeMapper.selectById(nodeId)).thenReturn(expectedNode);

        // When
        LearningPathNode result = learningPathService.getLearningPathNodeById(nodeId);

        // Then
        assertNotNull(result);
        assertEquals(nodeId, result.getId());
        assertEquals(LearningPathNode.NODE_STATUS_PROCEEDING, result.getStatus());
    }

    @Test
    void testUpdateLearningPathNodeStatus() {
        // Given
        Long nodeId = 1L;
        String status = LearningPathNode.NODE_STATUS_COMPLETED;

        // When
        learningPathService.updateLearningPathNodeStatus(nodeId, status);

        // Then
        verify(learningPathNodeMapper).updateStatus(nodeId, status);
    }

    @Test
    void testUpdateLearningPathStage() {
        // Given
        Long pathId = 1L;
        String stage = LearningPath.STAGE_TESTING;

        // When
        learningPathService.updateLearningPathStage(pathId, stage);

        // Then
        verify(learningPathMapper).updateCurrentStage(pathId, stage);
    }

    @Test
    void testUpdateLearningPath() {
        // Given
        LearningPath path = new LearningPath(1L, studentId, courseId, 3L,
                LearningPath.STAGE_FINISHED, "已完成", LocalDateTime.now(), LocalDateTime.now());

        // When
        learningPathService.updateLearningPath(path);

        // Then
        verify(learningPathMapper).update(path);
    }

    @Test
    void testGenerateCandidateKnowledgeInfo_WithData() {
        // Given - 使用反射调用私有方法（或者可以通过公开的createLearningPath间接测试）
        Map<SectionKnowledgeRel, StudentKnowledgeMastery> knowledgeMap = new HashMap<>();

        SectionKnowledgeRel rel = new SectionKnowledgeRel(1L, courseId, 10L, "kp_1", LocalDateTime.now());
        StudentKnowledgeMastery mastery = new StudentKnowledgeMastery();
        mastery.setKnowledgeName("Java基础");
        mastery.setMasterRate(0.75);

        knowledgeMap.put(rel, mastery);

        // When - 使用反射访问私有方法
        try {
            java.lang.reflect.Method method = LearningPathServiceImpl.class
                    .getDeclaredMethod("generateCandidateKnowledgeInfo", Map.class);
            method.setAccessible(true);
            String result = (String) method.invoke(learningPathService, knowledgeMap);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("Java基础"));
            assertTrue(result.contains("75.0%"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testCreateLearningPath_WithNullMasteryRecords() {
        // Given - 学生没有任何学习记录
        when(sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(eq(courseId), eq(0L), eq(35)))
                .thenReturn(knowledgeRels.subList(0, 35));

        // 所有知识点都返回null（无学习记录）
        when(studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(eq(studentId), anyString()))
                .thenReturn(null);

        String aiResponse = "{\"studentId\":1,\"courseId\":100,\"additionalInfo\":\"新学生学习路径\"," +
                "\"pathNodeDataList\":[" +
                "{\"nodeName\":\"入门节点\",\"knowledgeIds\":[\"kp_0\",\"kp_1\",\"kp_2\"]}," +
                "{\"nodeName\":\"进阶节点\",\"knowledgeIds\":[\"kp_3\",\"kp_4\"]}]}";

        when(learningPathGenerateModel.call(eq(studentId), eq(courseId), anyString()))
                .thenReturn(aiResponse);

        LearningPath oldPath = new LearningPath(1L, studentId, courseId, null,
                LearningPath.STAGE_INTRODUCING, null, null, null);
        when(learningPathMapper.selectByStudentAndCourse(studentId, courseId))
                .thenReturn(oldPath);

        // When
        learningPathService.createLearningPath(studentId, courseId);

        // Then - 验证即使没有学习记录也能正常生成路径
        verify(learningPathNodeMapper, times(2)).insert(any(LearningPathNode.class));
    }
}