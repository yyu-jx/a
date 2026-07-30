package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.StudentCourseProgressesMapper;
import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;
import com.superyoudi.businessservice.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HomeserviceImpl implements HomeService {


    private final StudentCourseProgressesMapper studentCourseProgressesMapper;

    /**
     * 获取学习概览
     *
     * @param userId 用户ID
     * @return 学习概览
     */
    @Override
    public StudyOverviewVO getStudyOverview(Integer userId){
        //拿到学习概览数据
        StudyOverviewVO vo=studentCourseProgressesMapper.getAvgProgresses(userId);
        //如果课程总数为0，则吧其他数据设置为0
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
