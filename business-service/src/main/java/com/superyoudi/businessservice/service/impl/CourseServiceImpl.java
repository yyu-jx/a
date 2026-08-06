package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.ai.agent.CourseDataGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.dto.CourseDataDTO;
import com.superyoudi.businessservice.pojo.entity.Chapter;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.entity.Section;
import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import com.superyoudi.businessservice.pojo.vo.CourseVO;
import com.superyoudi.businessservice.pojo.vo.StudentCourseProgressVO;
import com.superyoudi.businessservice.pojo.vo.TodayTaskVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.common.exception.StudentRepeatJoinCourseException;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import com.superyoudi.kdbservice.repository.KnowledgePointRepository;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private final KnowledgePointRepository knowledgePointRepository;
    private final SectionKnowledgeRelMapper sectionKnowledgeRelMapper;
    private final KnowledgeGraphService knowledgeGraphService;

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Override
    public CourseVO getCourseInfo(Long courseId) throws CourseNotExistException {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            // 抛出业务异常：课程不存在
            throw new CourseNotExistException("课程不存在");
        }

        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);

        return vo;
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
        Course course = courseMapper.selectCourseById(courseId);
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
     * 开课，调用AI生成课程数据
     */
    @Override
    public void openCourse(Long courseId) throws CourseNotExistException {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new CourseNotExistException("课程不存在");

        // ！！！description至少要包含出版社信息！！！
        String courseDataJson = courseDataGenerateModel.call(courseId, course.getName(), course.getDescription());
        CourseDataDTO courseData = AiMessageUtils.readJsonValue(courseDataJson, CourseDataDTO.class);

        if (courseData != null) {
            saveCourseData(courseData);
        } else {
            log.error("课程数据生成失败，courseDataJson:{}", courseDataJson);
            throw new RuntimeException("课程数据生成失败");
        }
    }

    private void saveCourseData(CourseDataDTO courseData) {
        for (CourseDataDTO.ChapterData chapterData : courseData.getChapterDataList()) {
            Chapter chapter = new Chapter(null, courseData.getCourseId(), chapterData.getChapterName(),
                    null, chapterData.getOrder(), null, null);
            Long chapterId = chapterMapper.insert(chapter);

            for (CourseDataDTO.SectionData sectionData : chapterData.getSectionDataList()) {
                Section section = new Section(null, chapterId, sectionData.getSectionName(),
                        null, sectionData.getOrder(), null, null);
                Long sectionId = sectionMapper.insert(section);

                for (KnowledgeExtractionResult extractionResult : sectionData.getExtractionResults()) {
                    List<KnowledgePointNode> knowledgePointNodes = knowledgeGraphService.saveKnowledgePoints(extractionResult);

                    for (KnowledgePointNode knowledgePointNode : knowledgePointNodes) {
                        SectionKnowledgeRel sectionKnowledgeRel = new SectionKnowledgeRel(null, sectionId, knowledgePointNode.getName(), null);
                        sectionKnowledgeRelMapper.insert(sectionKnowledgeRel);
                    }
                }
            }
        }
        log.info("课程数据生成成功，courseData:{}", courseData);
    }

}
