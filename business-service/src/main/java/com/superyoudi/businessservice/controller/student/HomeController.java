package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;
import com.superyoudi.businessservice.result.Result;
import com.superyoudi.businessservice.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 获取整体学习进度均值
     *
     * @param userId
     * @return 整体学习进度均值
     * @author gaoyuxin
     */
    @GetMapping("/study/overview")
    public Result<StudyOverviewVO> studyOverview(Integer userId) {
        StudyOverviewVO studyOverviewVO=homeService.getStudyOverview(userId);
        return Result.success(studyOverviewVO);
    }

}
