package com.superyoudi.businessservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.ai.agent.LearningPathGenerateModel;
import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.dto.LearningPathDataDTO;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.LearningPathService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.SectionKnowledgeRelMapper;
import com.superyoudi.businessservice.mapper.StudentKnowledgeMasteryMapper;
import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import com.superyoudi.common.dto.StudentKnowledgeMastery;
import com.superyoudi.common.exception.AllCourseKnowledgeMasteredException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class LearningPathServiceImpl implements LearningPathService {

    /// 每个学习路径包含的知识点数量, 每个节点大约3-5个，算上冗余，7*5=35
    private final int KNOWLEDGE_POINTS_EACH_PATH = 35;

    /// 创建学习路径的最大重试次数
    private final int CREATE_LEARNING_PATH_MAX_RETRIES = 10;

    private final LearningPathMapper learningPathMapper;
    private final LearningPathNodeMapper learningPathNodeMapper;
    private final SectionKnowledgeRelMapper sectionKnowledgeRelMapper;
    private final StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;
    private final LearningPathGenerateModel learningPathGenerateModel;
    private final CourseMapper courseMapper;

    public LearningPathServiceImpl(LearningPathMapper learningPathMapper, LearningPathNodeMapper learningPathNodeMapper, SectionKnowledgeRelMapper sectionKnowledgeRelMapper, StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper, LearningPathGenerateModel learningPathGenerateModel, CourseMapper courseMapper) {
        this.learningPathMapper = learningPathMapper;
        this.learningPathNodeMapper = learningPathNodeMapper;
        this.sectionKnowledgeRelMapper = sectionKnowledgeRelMapper;
        this.studentKnowledgeMasteryMapper = studentKnowledgeMasteryMapper;
        this.learningPathGenerateModel = learningPathGenerateModel;
        this.courseMapper = courseMapper;
    }

    /// 生成并覆盖旧的学习路径
    ///
    /// 拿所有课程知识点，默认已经按照前置知识排序。以掌握度低/无学习记录的知识点作为开始的知识点。
    ///
    /// 向后找一定数量的知识点，滤除已经掌握的知识点。
    ///
    /// 使用AI将这些知识点聚集成知识节点，并生成Json。若AI认为知识点的数量不够生成五个节点，则只输出最多已生成的节点，
    ///
    /// 完成后，生成学习路径，并保存到数据库。
    ///
    /// @return 新建的LearningPath的ID
    /// @author Sijayyx
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLearningPath(@NonNull Long studentId, @NonNull Long courseId) {

        Assert.notNull(studentId, "createLearningPath: studentId不能为null");
        Assert.notNull(courseId, "createLearningPath: courseId不能为null");

        // 候选知识点
        Map<SectionKnowledgeRel, StudentKnowledgeMastery> unmasteredKnowledgeMap = new HashMap<>();

        Long courseDataId = courseMapper.getCourseDataId(courseId);

        if (courseDataId == null) {
            log.error("课程数据获取失败，无法生成学习路径");
            throw new NullPointerException("课程数据获取失败，无法生成学习路径");
        }

        long startRelId = 0L;
        for (int i = 0; i < CREATE_LEARNING_PATH_MAX_RETRIES; i++) {
            Map<SectionKnowledgeRel, StudentKnowledgeMastery> candidateMap = new HashMap<>();
            List<SectionKnowledgeRel> sectionKnowledgeRels = sectionKnowledgeRelMapper.selectBehindStartByCourseDataId(courseDataId, startRelId, KNOWLEDGE_POINTS_EACH_PATH);

            for (SectionKnowledgeRel sectionKnowledgeRel : sectionKnowledgeRels) {
                StudentKnowledgeMastery studentKnowledgeMastery =
                        studentKnowledgeMasteryMapper.selectByStudentAndKnowledge(studentId, sectionKnowledgeRel.getNeo4jKpId());

                // 是否未掌握？
                boolean isUnmastered = studentKnowledgeMastery == null || studentKnowledgeMastery.getMasterRate() < 0.6;

                if (!isUnmastered) continue;

                // 无起始知识点，把第一个未掌握/无记录的知识点作为起始。
                if (startRelId == 0L)
                    startRelId = sectionKnowledgeRel.getId();

                // 找到了起始知识点后再添加候选知识点
                if (startRelId != 0L)
                    candidateMap.put(sectionKnowledgeRel, studentKnowledgeMastery);
            }

            // 将候选知识点的东西放到正式知识点中去
            unmasteredKnowledgeMap.putAll(candidateMap);

            // 检查知识点数量是否足够
            if (unmasteredKnowledgeMap.size() >= KNOWLEDGE_POINTS_EACH_PATH) break;
        }

        // 如果所有知识点都完成了，就抛异常
        if (unmasteredKnowledgeMap.isEmpty()) {
            log.warn("所有知识点均掌握，无法生成学习路径");
            throw new AllCourseKnowledgeMasteredException("没有找到未掌握的知识点");
        }

        String json = learningPathGenerateModel.call(studentId, courseId, generateCandidateKnowledgeInfo(unmasteredKnowledgeMap));

        LearningPathDataDTO lpData = AiMessageUtils.readJsonValue(json, LearningPathDataDTO.class);

        if (lpData == null) {
            log.error("学习路径数据解析失败。");
            throw new NullPointerException("学习路径数据解析失败。");
        }

        // 删除已有的学习路径
        LearningPath learningPath = learningPathMapper.selectByStudentAndCourse(studentId, courseId);
        if (learningPath != null) {
            learningPathNodeMapper.deleteAllByPathId(learningPath.getId());
            learningPathMapper.deleteById(learningPath.getId());
        }

        // 保存新的学习路径
        LearningPath newPath = new LearningPath(null, studentId, courseId, null, LearningPath.STAGE_INTRODUCING,
                lpData.getAdditionalInfo(), null, null);
        learningPathMapper.insert(newPath);

        ObjectMapper mapper = new ObjectMapper();

        for (LearningPathDataDTO.PathNodeData pathNodeData : lpData.getPathNodeDataList()) {
            String kpIdListJson;
            try {
                // 转换JSON字符串
                kpIdListJson = mapper.writeValueAsString(pathNodeData.getKnowledgeIds());
            } catch (JsonProcessingException e) {
                log.error("JSON转换失败, {}", pathNodeData.getKnowledgeIds());
                throw new RuntimeException("JSON转换失败！", e);
            }

            LearningPathNode node = new LearningPathNode(null, newPath.getId(), pathNodeData.getNodeName(),
                    kpIdListJson, LearningPathNode.NODE_STATUS_UNTESTED, null, null, null);

            learningPathNodeMapper.insert(node);
        }

        log.info("知识节点构建完成。一共 {} 个节点。", lpData.getPathNodeDataList().size());

        return newPath.getId();
    }

    /**
     * 生成学生的知识点掌握情况的提示词
     */
    private String generateCandidateKnowledgeInfo(Map<SectionKnowledgeRel, StudentKnowledgeMastery> candidateKnowledgeMap) {
        if (candidateKnowledgeMap == null || candidateKnowledgeMap.isEmpty()) {
            return "无知识点掌握数据";
        }
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<SectionKnowledgeRel, StudentKnowledgeMastery> entry : candidateKnowledgeMap.entrySet()) {
            StudentKnowledgeMastery mastery = entry.getValue();
            SectionKnowledgeRel rel = entry.getKey();

            String knowledgeName;
            String rateStr;

            if (mastery == null) {
                // 无学习记录，使用知识点ID作为名称
                knowledgeName = rel != null ? "知识点ID: " + rel.getNeo4jKpId() : "未知知识点";
                rateStr = "无记录";
            } else {
                knowledgeName = mastery.getKnowledgeName();
                Double masterRate = mastery.getMasterRate();
                rateStr = masterRate != null ? String.format("%.1f%%", masterRate * 100) : "未知";
            }

            sb.append("知识点: ").append(knowledgeName)
                    .append(", 学生掌握度: ").append(rateStr)
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * 根据学生和课程获取学习路径节点
     */
    @Override
    public List<LearningPathNode> getLearningPathNodes(Long studentId, Long courseId) {
        LearningPath learningPath = learningPathMapper.selectByStudentAndCourse(studentId, courseId);
        return learningPathNodeMapper.selectByPathId(learningPath.getId());
    }

    /**
     * 根据学生和课程获取学习路径节点
     */
    @Override
    public List<LearningPathNode> getLearningPathNodes(Long pathId) {
        return learningPathNodeMapper.selectByPathId(pathId);
    }

    @Override
    public Long getNextLearningPathNode(Long pathId, Long currentNodeId) {
        return learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId);
    }

    @Override
    public LearningPath getLearningPathById(Long learningPathId) {
        return learningPathMapper.selectById(learningPathId);
    }

    @Override
    public LearningPath getLearningPathByStudentAndCourse(Long studentId, Long courseId) {
        return learningPathMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public LearningPathNode getLearningPathNodeById(Long nodeId) {
        return learningPathNodeMapper.selectById(nodeId);
    }

    @Override
    public void updateLearningPathNodeStatus(Long nodeId, String status) {
        learningPathNodeMapper.updateStatus(nodeId, status);
    }

    @Override
    public void updateLearningPathStage(Long pathId, String stage) {
        learningPathMapper.updateCurrentStage(pathId, stage);
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        learningPathMapper.update(learningPath);
    }
}
