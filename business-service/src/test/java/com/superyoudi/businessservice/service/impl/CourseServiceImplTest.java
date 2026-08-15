package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.ai.agent.CourseDataGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.ChapterMapper;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.SectionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.SectionMapper;
import com.superyoudi.businessservice.pojo.dto.CourseOpeningDataDTO;
import com.superyoudi.businessservice.pojo.entity.Chapter;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.entity.Section;
import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import com.superyoudi.businessservice.pojo.vo.CourseVO;
import com.superyoudi.businessservice.pojo.vo.TodayTaskVO;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
public class CourseServiceImplTest {

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
    private KnowledgeGraphService knowledgeGraphService;
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

    @BeforeEach
    void setUp() {
        // 设置通用的stubbing
        when(chapterMapper.insert(any(Chapter.class))).thenReturn(1L);
        when(sectionMapper.insert(any(Section.class))).thenReturn(1L);

        // 模拟knowledgeGraphService.saveKnowledgePoints返回知识点列表
        when(knowledgeGraphService.saveKnowledgePoints(any(KnowledgeExtractionResult.class)))
                .thenAnswer(invocation -> {
                    KnowledgeExtractionResult result = invocation.getArgument(0);
                    List<KnowledgePointNode> nodes = new ArrayList<>();
                    if (result.getKnowledgePoints() != null) {
                        for (KnowledgeExtractionResult.KnowledgePointItem item : result.getKnowledgePoints()) {
                            KnowledgePointNode node = new KnowledgePointNode(item.getName(), item.getDescription());
                            nodes.add(node);
                        }
                    }
                    return nodes;
                });

        when(sectionKnowledgeRelMapper.insert(any(SectionKnowledgeRel.class)))
                .thenReturn(1L);
    }

    @Test
    void openCourse_Success() throws CourseNotExistException {
        // 1. 准备测试数据
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        // 2. 模拟AI生成的JSON数据 - 所有字段使用"xxx"
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
                "        \"extractionResults\": [" +
                "          {" +
                "            \"knowledgePoints\": [" +
                "              {\"name\": \"xxx\", \"description\": \"xxx\"}," +
                "              {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "            ]," +
                "            \"knowledgeRelations\": [" +
                "              {\"relation\": \"xxx\", \"sourceKnowledgePoint\": \"xxx\", \"targetKnowledgePoint\": \"xxx\"}" +
                "            ]" +
                "          }," +
                "          {" +
                "            \"knowledgePoints\": [" +
                "              {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "            ]," +
                "            \"knowledgeRelations\": []" +
                "          }" +
                "        ]" +
                "      }," +
                "      {" +
                "        \"sectionName\": \"xxx\"," +
                "        \"order\": 2," +
                "        \"extractionResults\": [" +
                "          {" +
                "            \"knowledgePoints\": [" +
                "              {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "            ]," +
                "            \"knowledgeRelations\": [" +
                "              {\"relation\": \"xxx\", \"sourceKnowledgePoint\": \"xxx\", \"targetKnowledgePoint\": \"xxx\"}" +
                "            ]" +
                "          }" +
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
                "        \"extractionResults\": [" +
                "          {" +
                "            \"knowledgePoints\": [" +
                "              {\"name\": \"xxx\", \"description\": \"xxx\"}" +
                "            ]," +
                "            \"knowledgeRelations\": []" +
                "          }" +
                "        ]" +
                "      }" +
                "    ]" +
                "  }" +
                "]" +
                "}";

        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(mockCourseDataJson);

        // 3. 构建CourseDataDTO对象
        CourseOpeningDataDTO mockCourseData = buildMockCourseData();

        // 4. Mock AiMessageUtils.readJsonValue (静态方法)
        try (MockedStatic<AiMessageUtils> utilities = mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(mockCourseDataJson, CourseOpeningDataDTO.class))
                    .thenReturn(mockCourseData);

            // 5. 执行测试
            courseService.generateCourseData(COURSE_ID);

            // 6. 验证结果
            verify(courseMapper).selectById(COURSE_ID);
            verify(courseDataGenerateModel).call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION);

            // 验证保存了2个章节
            verify(chapterMapper, times(2)).insert(any(Chapter.class));
            // 验证保存了3个小节
            verify(sectionMapper, times(3)).insert(any(Section.class));

            // 验证knowledgeGraphService.saveKnowledgePoints被调用了4次
            // 第1章第1节有2个extractionResults，第1章第2节有1个，第2章第1节有1个
            verify(knowledgeGraphService, times(4)).saveKnowledgePoints(any(KnowledgeExtractionResult.class));

            // 验证保存了知识点数量：第1节2个+第1节1个+第2节1个+第3节1个 = 5个
            verify(sectionKnowledgeRelMapper, times(5)).insert(any(SectionKnowledgeRel.class));
        }
    }

    @Test
    void openCourse_CourseNotExist_ThrowsException() {
        // 1. 模拟课程不存在
        when(courseMapper.selectById(COURSE_ID)).thenReturn(null);

        // 2. 执行测试并验证异常
        CourseNotExistException exception = assertThrows(
                CourseNotExistException.class,
                () -> courseService.generateCourseData(COURSE_ID)
        );

        assertEquals("课程不存在", exception.getMessage());
        verify(courseMapper).selectById(COURSE_ID);
        verify(courseDataGenerateModel, never()).call(anyLong(), anyString(), anyString());
        verify(chapterMapper, never()).insert(any(Chapter.class));
        verify(sectionMapper, never()).insert(any(Section.class));
        verify(knowledgeGraphService, never()).saveKnowledgePoints(any(KnowledgeExtractionResult.class));
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

        // 2. 模拟AI生成失败（返回无效数据）
        String invalidJson = "invalid json";
        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(invalidJson);

        // 3. 模拟AiMessageUtils.readJsonValue返回null
        try (MockedStatic<AiMessageUtils> utilities = mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(invalidJson, CourseOpeningDataDTO.class))
                    .thenReturn(null);

            // 4. 执行测试并验证异常
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> courseService.generateCourseData(COURSE_ID)
            );

            assertEquals("课程数据生成失败", exception.getMessage());
            verify(courseDataGenerateModel).call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION);
            verify(chapterMapper, never()).insert(any(Chapter.class));
            verify(sectionMapper, never()).insert(any(Section.class));
            verify(knowledgeGraphService, never()).saveKnowledgePoints(any(KnowledgeExtractionResult.class));
            verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
        }
    }

    @Test
    void openCourse_WithEmptyExtractionResults() throws CourseNotExistException {
        // 测试extractionResults为空的情况
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        // 最小化JSON数据 - extractionResults为空
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
                "        \"extractionResults\": []" +
                "      }" +
                "    ]" +
                "  }" +
                "]" +
                "}";

        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(minimalJson);

        CourseOpeningDataDTO minimalCourseData = buildMinimalCourseData();

        try (MockedStatic<AiMessageUtils> utilities = mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(minimalJson, CourseOpeningDataDTO.class))
                    .thenReturn(minimalCourseData);

            // 执行测试
            courseService.generateCourseData(COURSE_ID);

            // 验证：保存了1个章节和1个小节，但没有保存知识点
            verify(chapterMapper, times(1)).insert(any(Chapter.class));
            verify(sectionMapper, times(1)).insert(any(Section.class));
            verify(knowledgeGraphService, never()).saveKnowledgePoints(any(KnowledgeExtractionResult.class));
            verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
        }
    }

    @Test
    void openCourse_WithEmptyKnowledgePoints() throws CourseNotExistException {
        // 测试extractionResults存在但knowledgePoints为空的情况
        Course mockCourse = new Course();
        mockCourse.setId(COURSE_ID);
        mockCourse.setName(COURSE_NAME);
        mockCourse.setDescription(COURSE_DESCRIPTION);

        when(courseMapper.selectById(COURSE_ID)).thenReturn(mockCourse);

        String json = "{" +
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
                "        \"extractionResults\": [" +
                "          {" +
                "            \"knowledgePoints\": []," +
                "            \"knowledgeRelations\": []" +
                "          }" +
                "        ]" +
                "      }" +
                "    ]" +
                "  }" +
                "]" +
                "}";

        when(courseDataGenerateModel.call(COURSE_ID, COURSE_NAME, COURSE_DESCRIPTION))
                .thenReturn(json);

        CourseOpeningDataDTO courseData = buildCourseDataWithEmptyKnowledgePoints();

        try (MockedStatic<AiMessageUtils> utilities = mockStatic(AiMessageUtils.class)) {
            utilities.when(() -> AiMessageUtils.readJsonValue(json, CourseOpeningDataDTO.class))
                    .thenReturn(courseData);

            // 执行测试
            courseService.generateCourseData(COURSE_ID);

            // 验证：保存了1个章节和1个小节，但没有保存知识点
            verify(chapterMapper, times(1)).insert(any(Chapter.class));
            verify(sectionMapper, times(1)).insert(any(Section.class));
            verify(knowledgeGraphService, times(1)).saveKnowledgePoints(any(KnowledgeExtractionResult.class));
            verify(sectionKnowledgeRelMapper, never()).insert(any(SectionKnowledgeRel.class));
        }
    }

    /**
     * 构建模拟的CourseDataDTO对象 - 所有字段使用"xxx"
     */
    private CourseOpeningDataDTO buildMockCourseData() {
        CourseOpeningDataDTO courseData = new CourseOpeningDataDTO();
        courseData.setCourseId(COURSE_ID);
        courseData.setCourseName("xxx");

        List<CourseOpeningDataDTO.ChapterData> chapterList = new ArrayList<>();

        // 第1章
        CourseOpeningDataDTO.ChapterData chapter1 = new CourseOpeningDataDTO.ChapterData();
        chapter1.setChapterName("xxx");
        chapter1.setOrder(1);

        List<CourseOpeningDataDTO.SectionData> sectionList1 = new ArrayList<>();

        // 第1章第1节 - 包含2个extractionResults
        CourseOpeningDataDTO.SectionData section1 = new CourseOpeningDataDTO.SectionData();
        section1.setSectionName("xxx");
        section1.setOrder(1);

        List<KnowledgeExtractionResult> extractionResults1 = new ArrayList<>();

        // extractionResult 1 - 包含2个知识点和1个关系
        KnowledgeExtractionResult result1 = new KnowledgeExtractionResult();
        List<KnowledgeExtractionResult.KnowledgePointItem> knowledgePoints1 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgePointItem("xxx", "xxx"),
                new KnowledgeExtractionResult.KnowledgePointItem("xxx", "xxx")
        );
        List<KnowledgeExtractionResult.KnowledgeRelationItem> relations1 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgeRelationItem("xxx", "xxx", "xxx")
        );
        result1.setKnowledgePoints(knowledgePoints1);
        result1.setKnowledgeRelations(relations1);
        extractionResults1.add(result1);

        // extractionResult 2 - 包含1个知识点
        KnowledgeExtractionResult result2 = new KnowledgeExtractionResult();
        List<KnowledgeExtractionResult.KnowledgePointItem> knowledgePoints2 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgePointItem("xxx", "xxx")
        );
        result2.setKnowledgePoints(knowledgePoints2);
        result2.setKnowledgeRelations(new ArrayList<>());
        extractionResults1.add(result2);

        section1.setExtractionResults(extractionResults1);
        sectionList1.add(section1);

        // 第1章第2节 - 包含1个extractionResult
        CourseOpeningDataDTO.SectionData section2 = new CourseOpeningDataDTO.SectionData();
        section2.setSectionName("xxx");
        section2.setOrder(2);

        List<KnowledgeExtractionResult> extractionResults2 = new ArrayList<>();
        KnowledgeExtractionResult result3 = new KnowledgeExtractionResult();
        List<KnowledgeExtractionResult.KnowledgePointItem> knowledgePoints3 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgePointItem("xxx", "xxx")
        );
        List<KnowledgeExtractionResult.KnowledgeRelationItem> relations3 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgeRelationItem("xxx", "xxx", "xxx")
        );
        result3.setKnowledgePoints(knowledgePoints3);
        result3.setKnowledgeRelations(relations3);
        extractionResults2.add(result3);
        section2.setExtractionResults(extractionResults2);
        sectionList1.add(section2);

        chapter1.setSectionDataList(sectionList1);
        chapterList.add(chapter1);

        // 第2章
        CourseOpeningDataDTO.ChapterData chapter2 = new CourseOpeningDataDTO.ChapterData();
        chapter2.setChapterName("xxx");
        chapter2.setOrder(2);

        List<CourseOpeningDataDTO.SectionData> sectionList2 = new ArrayList<>();

        // 第2章第1节 - 包含1个extractionResult
        CourseOpeningDataDTO.SectionData section3 = new CourseOpeningDataDTO.SectionData();
        section3.setSectionName("xxx");
        section3.setOrder(1);

        List<KnowledgeExtractionResult> extractionResults3 = new ArrayList<>();
        KnowledgeExtractionResult result4 = new KnowledgeExtractionResult();
        List<KnowledgeExtractionResult.KnowledgePointItem> knowledgePoints4 = Arrays.asList(
                new KnowledgeExtractionResult.KnowledgePointItem("xxx", "xxx")
        );
        result4.setKnowledgePoints(knowledgePoints4);
        result4.setKnowledgeRelations(new ArrayList<>());
        extractionResults3.add(result4);
        section3.setExtractionResults(extractionResults3);
        sectionList2.add(section3);

        chapter2.setSectionDataList(sectionList2);
        chapterList.add(chapter2);

        courseData.setChapterDataList(chapterList);
        return courseData;
    }

    /**
     * 构建最小化的CourseDataDTO对象 - extractionResults为空
     */
    private CourseOpeningDataDTO buildMinimalCourseData() {
        CourseOpeningDataDTO courseData = new CourseOpeningDataDTO();
        courseData.setCourseId(COURSE_ID);
        courseData.setCourseName("xxx");

        List<CourseOpeningDataDTO.ChapterData> chapterList = new ArrayList<>();

        CourseOpeningDataDTO.ChapterData chapter = new CourseOpeningDataDTO.ChapterData();
        chapter.setChapterName("xxx");
        chapter.setOrder(1);

        List<CourseOpeningDataDTO.SectionData> sectionList = new ArrayList<>();
        CourseOpeningDataDTO.SectionData section = new CourseOpeningDataDTO.SectionData();
        section.setSectionName("xxx");
        section.setOrder(1);
        section.setExtractionResults(new ArrayList<>());
        sectionList.add(section);

        chapter.setSectionDataList(sectionList);
        chapterList.add(chapter);
        courseData.setChapterDataList(chapterList);

        return courseData;
    }

    /**
     * 构建包含空knowledgePoints的CourseDataDTO对象
     */
    private CourseOpeningDataDTO buildCourseDataWithEmptyKnowledgePoints() {
        CourseOpeningDataDTO courseData = new CourseOpeningDataDTO();
        courseData.setCourseId(COURSE_ID);
        courseData.setCourseName("xxx");

        List<CourseOpeningDataDTO.ChapterData> chapterList = new ArrayList<>();

        CourseOpeningDataDTO.ChapterData chapter = new CourseOpeningDataDTO.ChapterData();
        chapter.setChapterName("xxx");
        chapter.setOrder(1);

        List<CourseOpeningDataDTO.SectionData> sectionList = new ArrayList<>();
        CourseOpeningDataDTO.SectionData section = new CourseOpeningDataDTO.SectionData();
        section.setSectionName("xxx");
        section.setOrder(1);

        List<KnowledgeExtractionResult> extractionResults = new ArrayList<>();
        KnowledgeExtractionResult result = new KnowledgeExtractionResult();
        result.setKnowledgePoints(new ArrayList<>());
        result.setKnowledgeRelations(new ArrayList<>());
        extractionResults.add(result);
        section.setExtractionResults(extractionResults);
        sectionList.add(section);

        chapter.setSectionDataList(sectionList);
        chapterList.add(chapter);
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

        // 模拟mapper返回数据
        when(courseMapper.selectById(anyLong()))
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
        when(courseMapper.selectById(999l))
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
        when(knowledgePointService.batchGetKnowledgePoints(anyList())).thenReturn(knowledgeResult);


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
        when(knowledgePointService.batchGetKnowledgePoints(anyList())).thenReturn(List.of());

        List<TodayTaskVO> voResultList = courseService.getTodayTaskList(888L);

        assertEquals(1, voResultList.size());
        // 会出现null，业务最好做兜底处理！
        TodayTaskVO vo = voResultList.get(0);
        System.out.println(vo.getTaskTitle());
    }
}
