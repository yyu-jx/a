package com.superyoudi.businessservice;

import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.LearningPaths;
import com.superyoudi.businessservice.pojo.entity.StudentKnowledgeMastery;
import com.superyoudi.businessservice.pojo.entity.StudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.pojo.vo.WeakKnowledgeVO;
import com.superyoudi.businessservice.service.impl.HomeServiceImpl;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgeHomeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeServiceTest {

    @Mock
    private StudentCourseProgressMapper studentCourseProgressesMapper;

    @InjectMocks
    private HomeServiceImpl homeServiceImpl;

    @InjectMocks
    private HomeServiceImpl homeService;


    @Mock
    private LearningPathsMapper learningPathsMapper;
    @Mock
    private LearningPathNodesMapper learningPathNodesMapper;
    @Mock
    private KnowledgeHomeService knowledgeHomeService;
    @Mock
    private CoursesMapper coursesMapper;

    @Mock
    private KnowledgeMasteryMapper knowledgeMasteryMapper;


//获取整体学习进度均值接口
    /**
     * 用例1 用户有学习数据
     */
    @Test
    void getStudyOverview_HasData() {
        // 模拟mapper返回数据
        StudyOverview mockData = new StudyOverview();
        mockData.setStudyCourseTotal(3);
        mockData.setAvgTotalProgress(46.50);
        mockData.setAvgInteractionCount(32);
        mockData.setAvgTaskScore(75.25);
        mockData.setAvgKnowledgeCoverRate(51.30);
        mockData.setAvgTaskCompletionRate(58.70);

        when(studentCourseProgressesMapper.getAvgProgresses(any(Integer.class))).thenReturn(mockData);

        //执行真实HomeService里面业务方法
        StudyOverview result = homeServiceImpl.getStudyOverview(4);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getStudyCourseTotal());
        Assertions.assertEquals(46.50, result.getAvgTotalProgress());
        Assertions.assertEquals(32, result.getAvgInteractionCount());
        Assertions.assertEquals(75.25, result.getAvgTaskScore());
        Assertions.assertEquals(51.30, result.getAvgKnowledgeCoverRate());
        Assertions.assertEquals(58.70, result.getAvgTaskCompletionRate());
    }

    /**
     * 用例2 用户无学习记录，mapper返回null，service返回全部为0
     */
    @Test
    void getStudyOverview_NoData() {

        StudyOverview mockData = new StudyOverview();
        mockData.setStudyCourseTotal(0);
        mockData.setAvgTotalProgress(null);
        mockData.setAvgInteractionCount(null);
        mockData.setAvgTaskScore(null);
        mockData.setAvgKnowledgeCoverRate(null);
        mockData.setAvgTaskCompletionRate(null);

        when(studentCourseProgressesMapper.getAvgProgresses(any(Integer.class))).thenReturn(mockData);

        StudyOverview result = homeServiceImpl.getStudyOverview(10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getStudyCourseTotal());
        Assertions.assertEquals(0.0, result.getAvgTotalProgress());
        Assertions.assertEquals(0, result.getAvgInteractionCount());
        Assertions.assertEquals(0.0, result.getAvgTaskScore());
        Assertions.assertEquals(0.0, result.getAvgKnowledgeCoverRate());
        Assertions.assertEquals(0.0, result.getAvgTaskCompletionRate());
    }



 //获取上次学习记录接口

    // 场景1：用户没有任何学习记录，返回null
    @Test
    void getLatestStudyRecord_NoStudyRecord_ReturnNull() {
        Integer userId = 10001;
        when(learningPathsMapper.selectLatestPathByStudentId(userId)).thenReturn(null);

        LastStudyRecordVO vo = homeService.getLatestStudyRecord(userId);

        Assertions.assertNull(vo);
        verify(learningPathsMapper).selectLatestPathByStudentId(userId);
    }


    // 场景2：存在学习路径，有进行中的知识点节点，正常组装VO
    @Test
    void getLatestStudyRecord_HasProceedingNode_Success() {
        Integer userId = 10001;
        Long mockPathId = 101L;
        Long mockCourseId = 5001L;
        String mockKpId = "kp_008";
        LocalDateTime updateTime = LocalDateTime.now();

        LearningPaths mockPath = new LearningPaths();
        mockPath.setId(mockPathId);
        mockPath.setCourseId(mockCourseId);
        mockPath.setUpdatedAt(updateTime);

        KnowledgePointDTO dto = new KnowledgePointDTO();
        dto.setKnowledgeTitle("虚拟内存的基本概念");

        when(learningPathsMapper.selectLatestPathByStudentId(userId)).thenReturn(mockPath);
        when(learningPathNodesMapper.selectProceedingKnowledgeId(mockPathId)).thenReturn(mockKpId);
        when(learningPathNodesMapper.countUntestedNode(mockPathId)).thenReturn(2);
        when(knowledgeHomeService.batchGetKnowledge(List.of(mockKpId))).thenReturn(List.of(dto));
        when(coursesMapper.getNameById(mockCourseId)).thenReturn("操作系统 — 内存管理");

        LastStudyRecordVO vo = homeService.getLatestStudyRecord(userId);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals(mockPathId, vo.getPathId());
        Assertions.assertEquals(mockCourseId, vo.getCourseId());
        Assertions.assertEquals("操作系统 — 内存管理", vo.getCourseName());
        Assertions.assertEquals(mockKpId, vo.getCurrentKnowledgeId());
        Assertions.assertEquals("虚拟内存的基本概念", vo.getCurrentKnowledgeTitle());
        Assertions.assertEquals(2, vo.getRemainingUntestedCount());
        Assertions.assertEquals(updateTime.toString(), vo.getLastStudyTime());

        verify(learningPathNodesMapper, never()).selectLatestUpdateKnowledgeId(any());
    }

// 获取薄弱知识点接口

    /**
     * 场景1：用户存在学习数据，正常返回TOP5薄弱知识点
     */
    @Test
    void getTop5WeakKnowledge_HasData() {
        // 1. 构造mapper模拟返回数据
        StudentKnowledgeMastery mastery = new StudentKnowledgeMastery();
        mastery.setKnowledgeId("kp_022");
        mastery.setErrorRate(62.5);

        List<StudentKnowledgeMastery> mockRawList = List.of(mastery);

        // mock mapper查询
        when(knowledgeMasteryMapper.selectTop5WeakKnowledge(anyInt()))
                .thenReturn(mockRawList);

        // 构造批量查询知识点返回DTO
        KnowledgePointDTO dto = new KnowledgePointDTO();
        dto.setKnowledgeId("kp_022");
        dto.setKnowledgeTitle("虚拟内存");
        List<KnowledgePointDTO> mockDtoList = List.of(dto);

        when(knowledgeHomeService.batchGetKnowledge(anyList()))
                .thenReturn(mockDtoList);

        // 2. 执行方法
        List<WeakKnowledgeVO> result = homeService.getTop5WeakKnowledge(1);

        // 3. 断言校验
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        WeakKnowledgeVO vo = result.get(0);
        org.junit.jupiter.api.Assertions.assertEquals("kp_022", vo.getKnowledgeId());
        org.junit.jupiter.api.Assertions.assertEquals("虚拟内存", vo.getKnowledgeName());
        org.junit.jupiter.api.Assertions.assertEquals(62.5, vo.getErrorRate());
    }



    /**
     * 场景2：数据库没有知识点掌握数据，返回空集合
     */
    @Test
    void getTop5WeakKnowledge_EmptyData() {
        // mock返回空列表
        when(knowledgeMasteryMapper.selectTop5WeakKnowledge(anyInt()))
                .thenReturn(List.of());

        List<WeakKnowledgeVO> result = homeService.getTop5WeakKnowledge(1);

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }


}
