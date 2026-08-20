package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.pojo.vo.WeakKnowledgeVO;

import java.util.List;

public interface HomeService {
    /**
     * 获取最近一次学习记录
     *
     * @param userId 用户ID
     * @return 最近一次学习记录
     */
     LastStudyRecordVO getLatestStudyRecord(Long userId);

    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    StudentStudyOverview getStudyOverview(Long userId);

    /**
     * 获取薄弱知识点列表
     *
     * @param userId 用户ID
     * @return 薄弱知识点列表
     */
    List<WeakKnowledgeVO> getTop5WeakKnowledge(Long userId);

    /**
     * 学生端获取ai建议
     *
     * @param userId
     * @return 建议
     * @author gaoyuxin
     */
    String getAiSuggestions(Long userId);

    /**
     * 教师端获取ai建议
     *
     * @param teacherId
     * @return 建议
     * @author gaoyuxin
     */
    String generateSuggestions(Long teacherId);
}
