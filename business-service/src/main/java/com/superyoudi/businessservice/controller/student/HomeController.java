package com.superyoudi.businessservice.controller.student;

import com.superyoudi.businessservice.pojo.entity.Result;
import com.superyoudi.businessservice.pojo.entity.StudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.pojo.vo.WeakKnowledgeVO;
import com.superyoudi.businessservice.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

     /**
     * 获取最近学习记录
     *
     * @param userId
     * @return 最近学习记录
     * @author gaoyuxin
     */
    public Result<LastStudyRecordVO> getLatestStudyRecord(Integer userId) {
        LastStudyRecordVO vo = homeService.getLatestStudyRecord(userId);
        return Result.success(vo);
    }

    /**
     * 获取学生弱项TOP5知识点
     * 接口地址：GET /knowledge/weakests
     * @param userId 学生ID
     * @return 薄弱知识点列表
     */
    @GetMapping("/weakests")
    public Result<List<WeakKnowledgeVO>> getWeakestKnowledge(Integer userId) {
        List<WeakKnowledgeVO> result = homeService.getTop5WeakKnowledge(userId);
        return Result.success(result);
    }

}
