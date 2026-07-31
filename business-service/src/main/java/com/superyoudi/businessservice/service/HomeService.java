package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.StudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;

public interface HomeService {
    /**
     * 获取最近一次学习记录
     *
     * @param userId 用户ID
     * @return 最近一次学习记录
     */
     LastStudyRecordVO getLatestStudyRecord(Integer userId);

    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    StudyOverview getStudyOverview(Integer userId);
}
