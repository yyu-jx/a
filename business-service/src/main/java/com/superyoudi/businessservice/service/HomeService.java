package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.entity.StudyOverview;

public interface HomeService {
    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    StudyOverview getStudyOverview(Integer userId);
}
