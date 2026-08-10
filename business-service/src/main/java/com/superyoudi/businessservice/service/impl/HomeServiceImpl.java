package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.service.HomeAiService;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.mapper.StudentKnowledgeMasteryMapper;
import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.pojo.vo.WeakKnowledgeVO;
import com.superyoudi.businessservice.service.HomeService;
import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.dto.StudentKnowledgeMastery;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.superyoudi.common.constant.RedisConstant.AI_SUGGEST_CACHE_HOUR;
import static com.superyoudi.common.constant.RedisConstant.AI_SUGGEST_CACHE_KEY;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {


    private final StudentCourseProgressMapper studentCourseProgressesMapper;
    private final LearningPathMapper learningPathMapper;
    private final LearningPathNodeMapper learningPathNodeMapper;
    private final KnowledgePointService knowledgePointService;
    private final CourseMapper courseMapper;
    private final StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final HomeAiService homeAIService;


    /**
     * 获取最近一次学习记录
     *
     * @param userId 用户ID
     * @return 最近一次学习记录
     */
    @Override
    public LastStudyRecordVO getLatestStudyRecord(Long userId) {
        // ========== 步骤1：查询学生【最新的学习路径】按updated_at倒序取第一条 ==========
        LearningPath latestPath = learningPathMapper.selectLatestPathByStudentId(userId);
        if (latestPath == null) {
            // 无学习记录，可以返回null或者抛业务异常，前端展示「暂无学习记录」
            return null;
        }
        Long pathId = latestPath.getId();
        Long courseId = latestPath.getCourseId();

        // ========== 步骤2：查询当前知识点ID ==========
        // 优先查询 status = proceeding 进行中的节点
        String currentKnowledgeId = learningPathNodeMapper.selectProceedingKnowledgeId(pathId);
        // 如果没有进行中的节点 → 取更新时间最新的节点
        if (currentKnowledgeId == null) {
            currentKnowledgeId = learningPathNodeMapper.selectLatestUpdateKnowledgeId(pathId);
        }

        // ========== 步骤3：剩余未完成节点数量统计 status != completed ==========
        Integer remainingUntestedCount = learningPathNodeMapper.countUntestedNode(pathId);

        // ========== 步骤4：远程调用kdb-service 查询Neo4j知识点名称 ==========
        String knowledgeTitle = null;
        if (currentKnowledgeId != null) {
            try {
                List<KnowledgePointDTO> dtoList = knowledgePointService.batchGetKnowledgePoints(List.of(currentKnowledgeId));
                if (!dtoList.isEmpty()) {
                    knowledgeTitle = dtoList.get(0).getKnowledgeTitle();
                }
            } catch (Exception e) {
            }
        }
        // ========== 步骤5：查询课程名称 ==========
        String courseName = courseMapper.getNameById(courseId);

        // ========== 步骤6：组装VO返回 ==========
        LastStudyRecordVO vo = new LastStudyRecordVO();
        vo.setPathId(pathId);
        vo.setCourseId(courseId);
        vo.setCourseName(courseName);
        vo.setCurrentKnowledgeId(currentKnowledgeId);
        vo.setCurrentKnowledgeTitle(knowledgeTitle);
        vo.setRemainingUntestedCount(remainingUntestedCount);
        vo.setLastStudyTime(latestPath.getUpdatedAt().toString());
        return vo;
    }

    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    @Override
    public StudentStudyOverview getStudyOverview(Long userId) {
        //拿到学习概览数据
        StudentStudyOverview vo = studentCourseProgressesMapper.getAvgProgresses(userId);
        //如果课程总数为0，则吧其他数据设置为0
        if (vo.getStudyCourseTotal() == 0) {
            vo.setStudyCourseTotal(0);
            vo.setAvgTotalProgress(0.0);
            vo.setAvgInteractionCount(0);
            vo.setAvgTaskScore(0.0);
            vo.setAvgKnowledgeCoverRate(0.0);
            vo.setAvgTaskCompletionRate(0.0);
        }
        return vo;
    }

    /**
     * 获取学生弱项TOP5知识点
     *
     * @param userId 学生ID
     * @return 薄弱知识点列表
     */
    @Override
    public List<WeakKnowledgeVO> getTop5WeakKnowledge(Long userId) {
        List<StudentKnowledgeMastery> rawList = studentKnowledgeMasteryMapper.selectTopErrorKnowledge(userId, 5);
        if (rawList.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取知识点ID集合
        List<String> knowledgeIdList = rawList.stream()
                .map(StudentKnowledgeMastery::getKnowledgeId)
                .collect(Collectors.toList());

        // 调用你现有的批量方法
        List<KnowledgePointDTO> dtoList = knowledgePointService.batchGetKnowledgePoints(knowledgeIdList);

        // List 转 Map：key=知识点ID，value=知识点标题
        Map<String, String> knowledgeNameMap = dtoList.stream()
                .collect(Collectors.toMap(
                        KnowledgePointDTO::getKnowledgeId,
                        KnowledgePointDTO::getKnowledgeTitle,
                        // 重复id兼容（防止重复节点冲突）
                        (oldVal, newVal) -> newVal
                ));

        // 组装VO
        return rawList.stream().map(d -> {
            WeakKnowledgeVO vo = new WeakKnowledgeVO();
            vo.setKnowledgeId(d.getKnowledgeId());
            vo.setErrorRate(d.getErrorRate());
            // 取不到名称默认值
            vo.setKnowledgeName(knowledgeNameMap.getOrDefault(d.getKnowledgeId(), "未知知识点"));
            return vo;
        }).collect(Collectors.toList());
    }


    /**
     * 获取AI学习建议
     *
     * @param userId 用户ID
     * @return AI学习建议
     */
    @Override
    public String getAiSuggestions(Long userId) {
        // 1. 先查Redis缓存
        String cacheKey = AI_SUGGEST_CACHE_KEY + userId;
        String cacheText = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheText != null) {
            return cacheText;
        }

        // 1、查询学生TOP3薄弱知识点
        List<StudentKnowledgeMastery> weakKnowledgeList = studentKnowledgeMasteryMapper.selectTopErrorKnowledge(userId, 3);
        // 兜底：无薄弱知识点
        if (weakKnowledgeList == null || weakKnowledgeList.isEmpty()) {
            return "暂无学习薄弱数据，继续保持学习！";
        }

        // 2、提取薄弱知识点对应的courseId，去重
        List<Long> relateCourseIds = weakKnowledgeList.stream()
                .map(StudentKnowledgeMastery::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 3、查询【薄弱知识点所属课程】的进度
        List<StudentCourseProgress> progressList = List.of();
        if (!relateCourseIds.isEmpty()) {
            progressList = studentCourseProgressesMapper.selectProgressByStudentAndCourseIds(userId, relateCourseIds);
        }

        // 4、提取知识点ID，调用知识库批量查询知识点名称
        List<String> knowledgeIds = weakKnowledgeList.stream()
                .map(StudentKnowledgeMastery::getKnowledgeId)
                .collect(Collectors.toList());
        List<KnowledgePointDTO> dtoList = knowledgePointService.batchGetKnowledgePoints(knowledgeIds);

        // List 转 Map：key=知识点ID，value=知识点标题
        Map<String, String> knowledgeNameMap = dtoList.stream()
                .collect(Collectors.toMap(
                        KnowledgePointDTO::getKnowledgeId,
                        KnowledgePointDTO::getKnowledgeTitle,
                        // 重复id兼容（防止重复节点冲突）
                        (oldVal, newVal) -> newVal
                ));

        weakKnowledgeList = weakKnowledgeList.stream()
                .peek(mastery -> {
                    String name = knowledgeNameMap.getOrDefault(mastery.getKnowledgeId(), "未知知识点");
                    mastery.setKnowledgeName(name);
                })
                .collect(Collectors.toList());

        // 5、调用AI模块，生成学习建议
        String suggestion = homeAIService.generateStudentSuggest(progressList, weakKnowledgeList);

        // 8. 写入Redis缓存
        stringRedisTemplate.opsForValue().set(cacheKey, suggestion, AI_SUGGEST_CACHE_HOUR, TimeUnit.HOURS);

        return suggestion;
    }


}
