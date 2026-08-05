package com.superyoudi.businessservice;

import com.superyoudi.aiservice.ai.agent.CourseDataGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.ChapterMapper;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.SectionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.SectionMapper;
import com.superyoudi.businessservice.pojo.dto.CourseDataDTO;
import com.superyoudi.businessservice.pojo.entity.Chapter;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.entity.Section;
import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import com.superyoudi.businessservice.pojo.vo.CourseVO;
import com.superyoudi.businessservice.pojo.vo.TodayTaskVO;
import com.superyoudi.businessservice.service.impl.CourseServiceImpl;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 或者使用这个注解来放宽限制
public class CourseServiceTest {

    private static final Long COURSE_ID = 1L;
    private static final String COURSE_NAME = "测试课程";
    private static final String COURSE_DESCRIPTION = "测试课程描述，包含出版社信息：XXX出版社";
    // Mock Mapper 依赖
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private LearningPathMapper learningPathMapper;
    @Mock
    private KnowledgePointService knowledgePointService;
    @Mock
    private ChapterMapper chapterMapper;
    @Mock
    private SectionMapper sectionMapper;
    @Mock
    private KnowledgePointRepository knowledgePointRepository;
    @Mock
    private SectionKnowledgeRelMapper sectionKnowledgeRelMapper;
    @Mock
    private CourseDataGenerateModel courseDataGenerateModel;
    @InjectMocks
    private CourseServiceImpl courseService; // 假设您的服务类名为CourseService

    @Test
    void openCourse_Success() throws CourseNotExistException {
        // 1. 准备测试数据
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        // 2. 模拟AI生成的JSON数据
        String mockCourseDataJson = "{" +
                "\"courseId\": " + COURSE_ID + "," +
                "\"courseName\": \"xxx\"," +
                "\"chapterDataList\": [" +
                "  {" +
                "    \"chapterName\": \"xxx\"," +
                "    \"order\": 1," +
                "    \"sectionDataList\": [" +
                "      {" +
                "        \"sectionName\": \"xxx\"," +
                "        \"order\": 1," +
                "        \"knowledgePointDataList\": [" +
                "          {\"name\": \"xxx\", \"description\": \"xxx\"}," +
                "          {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "        ]" +
                "      }," +
                "      {" +
                "        \"sectionName\": \"xxx\"," +
                "        \"order\": 2," +
                "        \"knowledgePointDataList\": [" +
                "          {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "        ]" +
                "      }" +
                "    ]" +
                "  }," +
                "  {" +
                "    \"chapterName\": \"xxx\"," +
                "    \"order\": 2," +
                "    \"sectionDataList\": [" +
                "      {" +
                "        \"sectionName\": \"xxx\"," +
                "        \"order\": 1," +
                "        \"knowledgePointDataList\": [" +
                "          {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "        ]" +
                "      }" +
                "    ]" +
                "  }" +
                "]" +
                "}";

        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(mockCourseDataJson);

        // 3. Mock AiMessageUtils.readJsonValue - 如果无法mock静态方法，可以改为返回构造的CourseDataDTO对象
        CourseDataDTO mockCourseData = buildMockCourseData();

        try (MockedStatic<AiMessageUtils> utilities = Mockito.mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(mockCourseDataJson, CourseDataDTO.class))
                    .thenReturn(mockCourseData);

            // 4. Mock各个Mapper的insert方法
            // 第1章
            when(chapterMapper.insert(any(Chapter.class))).thenReturn(1L);
            // 第1章第1节
            when(sectionMapper.insert(any(Section.class))).thenReturn(1L);
            // 第1章第2节
            when(sectionMapper.insert(any(Section.class))).thenReturn(2L);
            // 第2章第1节
            when(sectionMapper.insert(any(Section.class))).thenReturn(3L);

            when(knowledgePointRepository.save(any(KnowledgePointNode.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(sectionKnowledgeRelMapper.insert(any(SectionKnowledgeRel.class)))
                    .thenReturn(1L);

            // 5. 执行测试
            courseService.openCourse(COURSE_ID);

            // 6. 验证结果
            verify(courseMapper).selectById(COURSE_ID);
            verify(courseDataGenerateModel).call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION);

            // 验证保存了2个章节
            verify(chapterMapper, times(2)).insert(any(Chapter.class));
            // 验证保存了3个小节（第1章有2节，第2章有1节）
            verify(sectionMapper, times(3)).insert(any(Section.class));
            // 验证保存了4个知识点（第1章第1节有2个，第1章第2节有1个，第2章第1节有1个）
            verify(knowledgePointRepository, times(4)).save(any(KnowledgePointNode.class));
            // 验证保存了4个章节-知识点关联
            verify(sectionKnowledgeRelMapper, times(4)).insert(any(SectionKnowledgeRel.class));

            // 验证日志 - 可以通过日志捕获验证，但这不是必需的
        }
    }

    @Test
    void openCourse_CourseNotExist_ThrowsException() {
        // 1. 模拟课程不存在
        when(courseMapper.selectById(COURSE_ID)).thenReturn(null);

        // 2. 执行测试并验证异常
        CourseNotExistException exception = assertThrows(
                CourseNotExistException.class,
                () -> courseService.openCourse(COURSE_ID)
        );

        assertEquals("课程不存在", exception.getMessage());
        verify(courseMapper).selectById(COURSE_ID);
        verify(courseDataGenerateModel, never()).call(anyLong(), anyString(), anyString());
        verify(chapterMapper, never()).insert(any(Chapter.class));
        verify(sectionMapper, never()).insert(any(Section.class));
        verify(knowledgePointRepository, never()).save(any(KnowledgePointNode.class));
        verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
    }

    @Test
    void openCourse_AiGenerationFailed_ThrowsException() throws CourseNotExistException {
        // 1. 准备测试数据
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        // 2. 模拟AI生成失败（返回null或空数据）
        String invalidJson = "invalid json";
        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(invalidJson);

        // 3. 模拟AiMessageUtils.readJsonValue返回null
        try (MockedStatic<AiMessageUtils> utilities = Mockito.mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(invalidJson, CourseDataDTO.class))
                    .thenReturn(null);

            // 4. 执行测试并验证异常
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> courseService.openCourse(COURSE_ID)
            );

            assertEquals("课程数据生成失败", exception.getMessage());
            verify(courseDataGenerateModel).call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION);
            verify(chapterMapper, never()).insert(any(Chapter.class));
            verify(sectionMapper, never()).insert(any(Section.class));
            verify(knowledgePointRepository, never()).save(any(KnowledgePointNode.class));
            verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
        }
    }

    @Test
    void openCourse_WithMinimalData() throws CourseNotExistException {
        // 测试只有最少数据的情况（没有知识点）
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        // 最小化JSON数据
        String minimalJson = "{" +
                "\"courseId\": " + COURSE_ID + "," +
                "\"courseName\": \"xxx\"," +
                "\"chapterDataList\": [" +
                "  {" +
                "    \"chapterName\": \"xxx\"," +
                "    \"order\": 1," +
                "    \"sectionDataList\": [" +
                "      {" +
                "        \"sectionName\": \"xxx\"," +
                "        \"order\": 1," +
                "        \"knowledgePointDataList\": []" +
                "      }" +
                "    ]" +
                "  }" +
                "]" +
                "}";

        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(minimalJson);

        CourseDataDTO minimalCourseData = new CourseDataDTO();
        minimalCourseData.setCourseId(COURSE_ID);
        minimalCourseData.setCourseName("xxx");

        List<CourseDataDTO.ChapterData> chapters = new ArrayList<>();
        CourseDataDTO.ChapterData chapter = new CourseDataDTO.ChapterData();
        chapter.setChapterName("xxx");
        chapter.setOrder(1);

        List<CourseDataDTO.SectionData> sections = new ArrayList<>();
        CourseDataDTO.SectionData section = new CourseDataDTO.SectionData();
        section.setSectionName("xxx");
        section.setOrder(1);
        section.setKnowledgePointDataList(new ArrayList<>());
        sections.add(section);
        chapter.setSectionDataList(sections);
        chapters.add(chapter);
        minimalCourseData.setChapterDataList(chapters);

        try (MockedStatic<AiMessageUtils> utilities = Mockito.mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(minimalJson, CourseDataDTO.class))
                    .thenReturn(minimalCourseData);

            when(chapterMapper.insert(any(Chapter.class))).thenReturn(1L);
            when(sectionMapper.insert(any(Section.class))).thenReturn(1L);

            // 执行测试
            courseService.openCourse(COURSE_ID);

            // 验证：保存了1个章节和1个小节，但没有保存知识点
            verify(chapterMapper, times(1)).insert(any(Chapter.class));
            verify(sectionMapper, times(1)).insert(any(Section.class));
            verify(knowledgePointRepository, never()).save(any(KnowledgePointNode.class));
            verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
        }
    }

    /**
     * 构建模拟的CourseDataDTO对象
     */
    private CourseDataDTO buildMockCourseData() {
        CourseDataDTO courseData = new CourseDataDTO();
        courseData.setCourseId(COURSE_ID);
        courseData.setCourseName("xxx");

        List<CourseDataDTO.ChapterData> chapterList = new ArrayList<>();

        // 第1章
        CourseDataDTO.ChapterData chapter1 = new CourseDataDTO.ChapterData();
        chapter1.setChapterName("xxx");
        chapter1.setOrder(1);

        List<CourseDataDTO.SectionData> sectionList1 = new ArrayList<>();

        // 第1章第1节
        CourseDataDTO.SectionData section1 = new CourseDataDTO.SectionData();
        section1.setSectionName("xxx");
        section1.setOrder(1);
        List<CourseDataDTO.KnowledgePointData> knowledgePoints1 = Arrays.asList(
                new CourseDataDTO.KnowledgePointData("xxx", "xxx"),
                new CourseDataDTO.KnowledgePointData("xxx", "xxx")
        );
        section1.setKnowledgePointDataList(knowledgePoints1);
        sectionList1.add(section1);

        // 第1章第2节
        CourseDataDTO.SectionData section2 = new CourseDataDTO.SectionData();
        section2.setSectionName("xxx");
        section2.setOrder(2);
        List<CourseDataDTO.KnowledgePointData> knowledgePoints2 = Arrays.asList(
                new CourseDataDTO.KnowledgePointData("xxx", "xxx")
        );
        section2.setKnowledgePointDataList(knowledgePoints2);
        sectionList1.add(section2);

        chapter1.setSectionDataList(sectionList1);
        chapterList.add(chapter1);

        // 第2章
        CourseDataDTO.ChapterData chapter2 = new CourseDataDTO.ChapterData();
        chapter2.setChapterName("xxx");
        chapter2.setOrder(2);

        List<CourseDataDTO.SectionData> sectionList2 = new ArrayList<>();

        // 第2章第1节
        CourseDataDTO.SectionData section3 = new CourseDataDTO.SectionData();
        section3.setSectionName("xxx");
        section3.setOrder(1);
        List<CourseDataDTO.KnowledgePointData> knowledgePoints3 = Arrays.asList(
                new CourseDataDTO.KnowledgePointData("xxx", "xxx")
        );
        section3.setKnowledgePointDataList(knowledgePoints3);
        sectionList2.add(section3);

        chapter2.setSectionDataList(sectionList2);
        chapterList.add(chapter2);

        courseData.setChapterDataList(chapterList);
        return courseData;
    }

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
        CourseVO vo = courseService.getCourseInfo(1l);

        // 3.断言校验
        Assertions.assertNotNull(vo);
        assertEquals(1, vo.getId());
        assertEquals("Java基础课程", vo.getName());
        assertEquals("张老师", vo.getTeacherName());
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
        CourseNotExistException exception = assertThrows(CourseNotExistException.class, () -> {
            courseService.getCourseInfo(999l);
        });
        assertEquals("课程不存在", exception.getMessage());
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
        assertThrows(NoStudyTaskException.class, () -> {
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
        assertEquals(1, voResultList.size());

        TodayTaskVO vo = voResultList.get(0);
        assertEquals(200, vo.getCourseId());
        assertEquals("k_001", vo.getKnowledgeId());
        assertEquals("proceeding", vo.getKnowledgeStatus());
        // taskTitle = 课程名 + "-" +知识点标题
        assertEquals("Java基础 - 变量与数据类型", vo.getTaskTitle());
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

        assertEquals(1, voResultList.size());
        // 会出现null，业务最好做兜底处理！
        TodayTaskVO vo = voResultList.get(0);
        System.out.println(vo.getTaskTitle());
    }
}
