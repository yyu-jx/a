package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;

public interface HomeService {
    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    StudyOverviewVO getStudyOverview(Integer userId);
}
