package com.superyoudi.businessservice;

import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.pojo.vo.TodayTaskVO;
import com.superyoudi.businessservice.service.impl.CourseServiceImpl;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    // Mock Mapper 依赖
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private LearningPathMapper learningPathMapper;
    @Mock
    private KnowledgePointService knowledgePointService;

    // 将mock注入到待测试Service
    @InjectMocks
    private CourseServiceImpl courseService;

    /**
     * 测试：课程存在，正常返回VO
     */
    @Test
    void getCourseInfo_Success() {
        // 1.构造模拟DO数据
        Course mockCourse = new Course();
        mockCourse.setId(1l);
        mockCourse.setName("Java基础课程");
        mockCourse.setTeacherName("张老师");

        // 模拟mapper返回数据
        when(courseMapper.selectCourseById(anyLong()))
                .thenReturn(mockCourse);

        // 2.执行方法
        CoursesVO vo = courseService.getCourseInfo(1l);

        // 3.断言校验
        Assertions.assertNotNull(vo);
        Assertions.assertEquals(1, vo.getId());
        Assertions.assertEquals("Java基础课程", vo.getName());
        Assertions.assertEquals("张老师", vo.getTeacherName());
    }

    /**
     * 测试：课程不存在，预期抛出异常
     */
    @Test
    void getCourseInfo_NotFound_ThrowException() {
        // mock返回null
        when(courseMapper.selectCourseById(999l))
                .thenReturn(null);

        // 断言抛出指定异常
        CourseNotExistException exception = Assertions.assertThrows(CourseNotExistException.class, () -> {
            courseService.getCourseInfo(999l);
        });
        Assertions.assertEquals("课程不存在", exception.getMessage());
    }

//今日任务生成
    /**
     * 场景1：没有任何学习任务，rawList为空，预期抛出自定义异常
     */
    @Test
    void getTodayTaskList_whenEmpty_throwException() {
        // given
        when(learningPathMapper.selectTaskRawByStudentId(anyLong())).thenReturn(List.of());

        // when & then
        Assertions.assertThrows(NoStudyTaskException.class, () -> {
            courseService.getTodayTaskList(1001L);
        });
    }


    /**
     * 场景2：正常拿到任务，完整链路测试，校验VO组装结果
     */
    @Test
    void getTodayTaskList_normal_success() {
        // ===== given 准备mock返回数据 =====
        TodayTaskRawDTO rawDto = new TodayTaskRawDTO();
        rawDto.setCourseId(200L);
        rawDto.setPathId(300L);
        rawDto.setKnowledgeId("k_001");
        rawDto.setStatus("proceeding");
        rawDto.setCourseName("Java基础");

        List<TodayTaskRawDTO> rawList = List.of(rawDto);
        when(learningPathMapper.selectTaskRawByStudentId(anyLong())).thenReturn(rawList);

        // mock知识库批量查询返回
        KnowledgePointDTO knowledgePointDTO = new KnowledgePointDTO();
        knowledgePointDTO.setKnowledgeId("k_001");
        knowledgePointDTO.setKnowledgeTitle("变量与数据类型");
        List<KnowledgePointDTO> knowledgeResult = List.of(knowledgePointDTO);
        when(knowledgePointService.batchGetKnowledge(anyList())).thenReturn(knowledgeResult);


        // ===== when 执行业务方法 =====
        List<TodayTaskVO> voResultList = courseService.getTodayTaskList(1001L);


        // ===== then 断言校验结果 =====
        Assertions.assertNotNull(voResultList);
        Assertions.assertEquals(1, voResultList.size());

        TodayTaskVO vo = voResultList.get(0);
        Assertions.assertEquals(200, vo.getCourseId());
        Assertions.assertEquals("k_001", vo.getKnowledgeId());
        Assertions.assertEquals("proceeding", vo.getKnowledgeStatus());
        // taskTitle = 课程名 + "-" +知识点标题
        Assertions.assertEquals("Java基础 - 变量与数据类型", vo.getTaskTitle());
    }


    /**
     * 场景3：数据库查到知识点ID，但是知识库查不到该知识点，map.get得到null
     * 【重点！你的代码这里会出现 taskTitle = "Java基础 - null"】
     */
    @Test
    void getTodayTaskList_knowledgeNameIsNull() {
        TodayTaskRawDTO rawDto = new TodayTaskRawDTO();
        rawDto.setCourseId(200L);
        rawDto.setKnowledgeId("k_999");
        rawDto.setStatus("untested");
        rawDto.setCourseName("MySQL教程");
        List<TodayTaskRawDTO> rawList = List.of(rawDto);

        when(learningPathMapper.selectTaskRawByStudentId(anyLong())).thenReturn(rawList);
        // 知识库返回空集合，map拿不到key，get返回null
        when(knowledgePointService.batchGetKnowledge(anyList())).thenReturn(List.of());

        List<TodayTaskVO> voResultList = courseService.getTodayTaskList(888L);

        Assertions.assertEquals(1, voResultList.size());
        // 会出现null，业务最好做兜底处理！
        TodayTaskVO vo = voResultList.get(0);
        System.out.println(vo.getTaskTitle());
    }
}
