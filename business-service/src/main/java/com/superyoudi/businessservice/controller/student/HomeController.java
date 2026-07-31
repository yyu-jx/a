package com.superyoudi.businessservice.controller.student;

import com.superyoudi.common.entity.Result;
import com.superyoudi.businessservice.pojo.entity.StudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.service.HomeService;
import lombok.RequiredArgsConstructor;
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
    public Result<StudyOverview> studyOverview(Integer userId) {
        StudyOverview studyOverviewVO=homeService.getStudyOverview(userId);
        return Result.success(studyOverviewVO);
    }

    @GetMapping("/latest")
    public Result<LastStudyRecordVO> getLatestStudyRecord(Integer userId) {
        LastStudyRecordVO vo = homeService.getLatestStudyRecord(userId);
        return Result.success(vo);
    }

}
