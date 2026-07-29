package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;

public interface HomeService {
    StudyOverviewVO getStudyOverview(Integer userId);
}
