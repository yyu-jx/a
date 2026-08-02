package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.StudyOverview;
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
    StudyOverview getStudyOverview(Long userId);

    /**
     * 获取薄弱知识点列表
     *
     * @param userId 用户ID
     * @return 薄弱知识点列表
     */
    List<WeakKnowledgeVO> getTop5WeakKnowledge(Long userId);
}
