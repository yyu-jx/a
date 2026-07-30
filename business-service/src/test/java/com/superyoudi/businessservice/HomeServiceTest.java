package com.superyoudi.businessservice;

import com.superyoudi.businessservice.mapper.StudentCourseProgressesMapper;
import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;
import com.superyoudi.businessservice.service.HomeService;
import com.superyoudi.businessservice.service.impl.HomeserviceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeServiceTest {

    @Mock
    private StudentCourseProgressesMapper studentCourseProgressesMapper;

    @InjectMocks
    private HomeserviceImpl homeServiceImpl;

    /**
     * 用例1 用户有学习数据
     */
    @Test
    void getStudyOverview_HasData() {
        // 模拟mapper返回数据
        StudyOverviewVO mockData = new StudyOverviewVO();
        mockData.setStudyCourseTotal(3);
        mockData.setAvgTotalProgress(46.50);
        mockData.setAvgInteractionCount(32);
        mockData.setAvgTaskScore(75.25);
        mockData.setAvgKnowledgeCoverRate(51.30);
        mockData.setAvgTaskCompletionRate(58.70);

        when(studentCourseProgressesMapper.getAvgProgresses(any(Integer.class))).thenReturn(mockData);

        //执行真实HomeService里面业务方法
        StudyOverviewVO result = homeServiceImpl.getStudyOverview(4);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getStudyCourseTotal());
        Assertions.assertEquals(46.50, result.getAvgTotalProgress());
        Assertions.assertEquals(32, result.getAvgInteractionCount());
        Assertions.assertEquals(75.25, result.getAvgTaskScore());
        Assertions.assertEquals(51.30, result.getAvgKnowledgeCoverRate());
        Assertions.assertEquals(58.70, result.getAvgTaskCompletionRate());
    }

    /**
     * 用例2 用户无学习记录，mapper返回null，service返回全部为0
     */
    @Test
    void getStudyOverview_NoData() {

        StudyOverviewVO mockData = new StudyOverviewVO();
        mockData.setStudyCourseTotal(0);
        mockData.setAvgTotalProgress(null);
        mockData.setAvgInteractionCount(null);
        mockData.setAvgTaskScore(null);
        mockData.setAvgKnowledgeCoverRate(null);
        mockData.setAvgTaskCompletionRate(null);

        when(studentCourseProgressesMapper.getAvgProgresses(any(Integer.class))).thenReturn(mockData);

        StudyOverviewVO result = homeServiceImpl.getStudyOverview(10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getStudyCourseTotal());
        Assertions.assertEquals(0.0, result.getAvgTotalProgress());
        Assertions.assertEquals(0, result.getAvgInteractionCount());
        Assertions.assertEquals(0.0, result.getAvgTaskScore());
        Assertions.assertEquals(0.0, result.getAvgKnowledgeCoverRate());
        Assertions.assertEquals(0.0, result.getAvgTaskCompletionRate());
    }

}
