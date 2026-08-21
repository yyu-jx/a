package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.ai.model.CourseDataGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.LearningPathService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.dto.CourseDTO;
import com.superyoudi.businessservice.pojo.dto.CourseOpeningDataDTO;
import com.superyoudi.businessservice.pojo.entity.*;
import com.superyoudi.businessservice.pojo.vo.*;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.constants.CourseConstants;
import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.common.exception.BusinessException;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final StudentCourseProgressMapper studentCourseProgressesMapper;
    private final KnowledgePointService knowledgeHomeService;
    private final LearningPathMapper learningPathMapper;
    private final CourseDataGenerateModel courseDataGenerateModel;
    private final ChapterMapper chapterMapper;
    private final SectionMapper sectionMapper;
    private final SectionKnowledgeRelMapper sectionKnowledgeRelMapper;
    private final KnowledgeGraphService knowledgeGraphService;
    private final LearningPathService learningPathService;
    private final TeacherCourseAnalysisMapper teacherCourseAnalysisMapper;
    private final CourseDataMapper courseDataMapper;
    private final UserMapper userMapper;

    /**
     * 获取全部课程数据表
     */
    @Override
    public List<CourseData> getCourseDataList() {
        return courseDataMapper.selectAll();
    }

    /**
     * 添加新课程
     */
    @Override
    public Long addNewCourse(@NonNull CourseDTO courseDTO) {
        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);

        // 验证是否存在引用的课程数据
        if (courseDataMapper.existsById(courseDTO.getCourseDataId()) == 0) {
            log.error("课程数据 {} 不存在", courseDTO.getCourseDataId());
            throw new CourseNotExistException("课程数据不存在");
        }

        course.setStatus(CourseConstants.STATUS_OPENING);

        courseMapper.insert(course);

        return course.getId();
    }

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Override
    public CourseVO getCourseInfo(Long courseId) throws CourseNotExistException {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            // 抛出业务异常：课程不存在
            throw new CourseNotExistException("课程不存在");
        }

        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);

        return vo;
    }

    @Override
    public Course getCourseById(Long courseId) {
        return courseMapper.selectById(courseId);
    }

    @Override
    public TeacherCourseAnalysis getTeacherCourseAnalysis(Long courseId) {
        return teacherCourseAnalysisMapper.selectByCourseId(courseId);
    }

    @Override
    public CourseStatisticsVO getCourseStatistics(Long teacherId) {
        int courseTotal = courseMapper.countCoursesByTeacherId(teacherId);
        int studentTotal = courseMapper.countStudentsByTeacherId(teacherId);
        Double avgMasterRate = teacherCourseAnalysisMapper.getTotalAvgProgress(teacherId);

        return new CourseStatisticsVO(courseTotal, studentTotal, avgMasterRate);
    }

    @Override
    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return courseMapper.selectByTeacherId(teacherId);
    }


    /**
     * 获取学生课程进度
     *
     * @param courseId  课程id
     * @param studentId 学生id
     * @return 学生课程进度
     */
    @Override
    public StudentCourseProgressVO getCourseProgress(Long courseId, Long studentId) {

        List<Long> courseIdList = List.of(courseId);
        List<StudentCourseProgress> progressList = studentCourseProgressesMapper.selectProgressByStudentAndCourseIds(studentId, courseIdList);
        if (CollectionUtils.isEmpty(progressList)) {
            throw new CourseNotExistException("课程不存在");
        }

        StudentCourseProgressVO vo = new StudentCourseProgressVO();
        BeanUtils.copyProperties(progressList.get(0), vo);

        return vo;
    }

    @Override
    public List<CourseStudyOverviewVO> getCourseStudyOverviews(Long teacherId) {
        List<Course> courses = courseMapper.selectByTeacherId(teacherId);

        List<TeacherCourseAnalysis> courseAnalyses = courses.stream()
                .map(course ->
                        teacherCourseAnalysisMapper.selectByCourseId(course.getId()))
                .filter(Objects::nonNull)
                .toList();

        // TODO 改接口文档
        return courseAnalyses.stream()
                .map(analysis -> {
                    Course course = courseMapper.selectById(analysis.getCourseId());
                    if (course == null) return null;

                    return new CourseStudyOverviewVO(course.getId(), course.getName(),
                            analysis.getAvgProgress().doubleValue(), analysis.getAvgScoreRate().doubleValue(),
                            analysis.getSubmissionRate().doubleValue(), analysis.getWeakestKnowledge());
                })
                .filter(Objects::nonNull)
                .toList();
    }


    /**
     * 加入课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     */
    @Override
    public void joinCourse(Long studentId, Long courseId) {
        // 1. 判断学生是否已经加入该课程
        Integer count = courseMapper.countStudentJoinCourse(studentId, courseId);
        if (count > 0) {
            throw new StudentRepeatJoinCourseException("您已加入该课程，请勿重复加入");
        }

        // 2. 校验课程是否真实存在
        Course course = courseMapper.selectById(courseId);
        if (Objects.isNull(course)) {
            throw new CourseNotExistException("课程不存在");
        }

        courseMapper.joinCourse(studentId, courseId);
    }

    /**
     * 获取今日任务列表
     *
     * @param userId 用户id
     * @return 今日任务列表
     */
    @Override
    public List<TodayTaskVO> getTodayTaskList(Long userId) {
        //拿到该学生每门课节点（proceeding优先，其次untested最新，过滤completed）
        List<TodayTaskRawDTO> rawList = learningPathMapper.selectTaskRawByStudentId(userId);
        if (rawList.isEmpty()) {
            throw new NoStudyTaskException("所有课程知识点均已完成");
        }

        // 收集所有知识点id，批量调用知识库
        List<String> knowledgeIdList = rawList.stream()
                .map(TodayTaskRawDTO::getKnowledgeId)
                .distinct()
                .collect(Collectors.toList());

        List<KnowledgePointDTO> knowledgelist = knowledgeHomeService.batchGetKnowledgePoints(knowledgeIdList);

        // List 转 Map：key=知识点ID，value=知识点标题
        Map<String, String> knowledgeNameMap = knowledgelist.stream()
                .collect(Collectors.toMap(
                        KnowledgePointDTO::getKnowledgeId,
                        KnowledgePointDTO::getKnowledgeTitle,
                        // 重复id兼容（防止重复节点冲突）
                        (oldVal, newVal) -> newVal
                ));

        // 内存组装VO
        return rawList.stream()
                .map(raw -> {
                    TodayTaskVO vo = new TodayTaskVO();
                    vo.setCourseId(raw.getCourseId().intValue());
                    vo.setKnowledgeId(raw.getKnowledgeId());
                    vo.setKnowledgeStatus(raw.getStatus());
                    vo.setTaskTitle(raw.getCourseName() + " - " + knowledgeNameMap.getOrDefault(raw.getKnowledgeId(), "【知识点已失效】"));
                    return vo;
                })
                .collect(Collectors.toList());

    }

    /**
     * 获取课程的学习路径。如果没有，就创建学习路径。
     */
    @Override
    public LearningPathVO getCourseLearningPath(Long studentId, Long courseId) {
        LearningPath learningPath = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);
        if (learningPath == null) {
            Long lpId = learningPathService.createLearningPath(studentId, courseId);
            learningPath = learningPathService.getLearningPathById(lpId);
        }

        List<LearningPathNode> learningPathNodes = learningPathService.getLearningPathNodes(learningPath.getId());

        List<LearningPathVO.PathNode> nodes = learningPathNodes.stream()
                .map(node -> new LearningPathVO.PathNode(
                        node.getId(), node.getNodeName(), node.getKnowledgeIds(), node.getStatus()))
                .toList();

        return new LearningPathVO(learningPath.getId(), courseId, learningPath.getAdditionalInfo(), nodes);
    }

    /**
     * 开课，调用AI生成课程数据
     */
    @Override
    @Transactional
    public void generateCourseData(Long courseDataId) throws CourseNotExistException {
        CourseData cd = courseDataMapper.selectById(courseDataId); // FIXME
        if (cd == null) throw new CourseNotExistException("课程数据不存在");

        String courseDataJson = courseDataGenerateModel.call(courseDataId, cd.getName(), cd.getInfo());
        CourseOpeningDataDTO courseData = AiMessageUtils.convertJsonValue(courseDataJson, CourseOpeningDataDTO.class);

        if (courseData != null) {
            saveCourseData(courseData);
        } else {
            log.error("课程数据生成失败，courseDataJson:{}", courseDataJson);
            throw new RuntimeException("课程数据生成失败");
        }
    }


    /**
     * 获取用户课程列表
     *
     * @param userId 用户id
     * @return 课程列表
     */
    @Override
    public List<CourseVO> getUserCourseList(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户角色
        String role = userMapper.selectUserRoleById(userId);
        if (role == null) {
            throw new BusinessException("用户角色不存在");
        }

        List<CourseVO> result;
        if ("teacher".equals(role)) {
            //教师角色，查授课课程
            result = courseMapper.selectTeacherCourseList(userId);
        } else if ("student".equals(role)) {
            //学生角色，查已加入课程
            result = courseMapper.selectStudentCourseList(userId);
        } else {
            // researcher等其他角色返回空集合
            result = new ArrayList<>();
        }


        return result;
    }

    public void saveCourseData(CourseOpeningDataDTO courseData) {
        List<KnowledgeExtractionResult.KnowledgePointItem> knowledgePoints = new ArrayList<>();
        List<KnowledgeExtractionResult.KnowledgeRelationItem> knowledgeRelations = new ArrayList<>();

        for (CourseOpeningDataDTO.ChapterData chapterData : courseData.getChapterDataList()) {
            Chapter chapter = new Chapter(null, courseData.getCourseDataId(), chapterData.getChapterName(),
                    null, chapterData.getOrder(), null, null);
            chapterMapper.insert(chapter);
            Long chapterId = chapter.getId();
            log.info(" chapterId:{}", chapterId);

            for (CourseOpeningDataDTO.SectionData sectionData : chapterData.getSectionDataList()) {
                Section section = new Section(null, chapterId, sectionData.getSectionName(),
                        null, sectionData.getOrder(), null, null);
                sectionMapper.insert(section);
                Long sectionId = section.getId();

                log.info("sectionId:{}, chapterId:{}", sectionId, chapterId);

                for (KnowledgeExtractionResult extractionResult : sectionData.getExtractionResults()) {

                    for (KnowledgeExtractionResult.KnowledgePointItem item : extractionResult.getKnowledgePoints()) {
                        SectionKnowledgeRel sectionKnowledgeRel =
                                new SectionKnowledgeRel(null, courseData.getCourseDataId(), sectionId, item.getName(), null);
                        sectionKnowledgeRelMapper.insert(sectionKnowledgeRel);
                    }

                    knowledgePoints.addAll(extractionResult.getKnowledgePoints());
                    knowledgeRelations.addAll(extractionResult.getKnowledgeRelations());
                }
            }
        }
        log.info("课程数据生成成功，courseData:{}", courseData);

        knowledgeGraphService.saveKnowledgePoints(new KnowledgeExtractionResult(knowledgePoints, knowledgeRelations));
    }

}
