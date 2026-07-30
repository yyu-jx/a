package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.vo.StudyOverviewVO;
import com.superyoudi.businessservice.entity.Result;
import com.superyoudi.businessservice.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HomeController {
    @Autowired
    private HomeService homeService;

    @GetMapping("/study/overview")
    public Result<StudyOverviewVO> studyOverview(Integer userId) {
        StudyOverviewVO studyOverviewVO=homeService.getStudyOverview(userId);
        return Result.success(studyOverviewVO);
    }

}
