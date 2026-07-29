package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.StudentCourseProgressesMapper;
import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;
import com.superyoudi.businessservice.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class HomeserviceImpl implements HomeService {

    @Autowired
    private StudentCourseProgressesMapper studentCourseProgressesMapper;

    @Override
    public StudyOverviewVO getStudyOverview(Integer userId){
        StudyOverviewVO vo=studentCourseProgressesMapper.getAvgProgresses(userId);
        if(vo.getStudyCourseTotal()==0){
            vo.setStudyCourseTotal(0);
            vo.setAvgTotalProgress(0.0);
            vo.setAvgInteractionCount(0);
            vo.setAvgTaskScore(0.0);
            vo.setAvgKnowledgeCoverRate(0.0);
            vo.setAvgTaskCompletionRate(0.0);
            return vo;
        }else {
            return vo;
        }
    }

}
