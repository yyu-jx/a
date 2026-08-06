package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.pojo.vo.StudentCourseProgressVO;
import com.superyoudi.businessservice.pojo.vo.TodayTaskVO;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import com.superyoudi.common.exception.CourseNotExistException;
import com.superyoudi.common.exception.NoStudyTaskException;
import com.superyoudi.common.exception.RepeatException;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final StudentCourseProgressMapper studentCourseProgressesMapper;
    private final KnowledgePointService knowledgeHomeService;
    private final LearningPathMapper learningPathMapper;

    /**
     * 获取单门课程详情
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Override
    public CoursesVO getCourseInfo(Long courseId) {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            // 抛出业务异常：课程不存在
            throw new CourseNotExistException("课程不存在");
        }

        CoursesVO vo = new CoursesVO();
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
            throw new RepeatException("您已加入该课程，请勿重复加入");
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

        List<KnowledgePointDTO> knowledgelist = knowledgeHomeService.batchGetKnowledge(knowledgeIdList);

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
}
