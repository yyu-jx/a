package com.superyoudi.businessservice.controller.student;

import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.businessservice.pojo.vo.LastStudyRecordVO;
import com.superyoudi.businessservice.pojo.vo.WeakKnowledgeVO;
import com.superyoudi.businessservice.service.HomeService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final ChatPersistenceService chatPersistenceService;

    /**
     * 获取整体学习进度均值
     *
     * @param userId
     * @return 整体学习进度均值
     * @author gaoyuxin
     */
    @GetMapping("/study/overview")
    public Result<StudentStudyOverview> studyOverview(Long userId) {
        StudentStudyOverview studentStudyOverviewVO = homeService.getStudyOverview(userId);
        return Result.success(studentStudyOverviewVO);
    }

    /**
     * 获取最近学习记录
     *
     * @param userId
     * @return 最近学习记录
     * @author gaoyuxin
     */
    public Result<LastStudyRecordVO> getLatestStudyRecord(Long userId) {
        LastStudyRecordVO vo = homeService.getLatestStudyRecord(userId);
        return Result.success(vo);
    }

    /**
     * 获取最薄弱知识点
     *
     * @param userId
     * @return 最薄弱知识点
     * @author gaoyuxin
     */
    @GetMapping("/weakests")
    public Result<List<WeakKnowledgeVO>> getWeakestKnowledge(Long userId) {
        List<WeakKnowledgeVO> result = homeService.getTop5WeakKnowledge(userId);
        return Result.success(result);
    }

    @GetMapping("/ai/suggestions/student")
    public Result<String> getAiSuggestions(Long userId) {
        String aiSuggestions = homeService.getAiSuggestions(userId);
        return Result.success(aiSuggestions);
    }

    /**
     * 获取AI的引用信息
     */
    // FIXME: 修改接口位置
    @GetMapping("/ai/citication")
    public Result<List<AiCitation>> getAiCitation(
            @RequestParam(required = false) Long messageId,
            @RequestParam(required = false) Long suggestionsId
    ) {
        if (messageId != null && suggestionsId != null)
            return Result.error("messageId 和 suggestionsId 不能同时提供");

        if (messageId != null) {
            return Result.success(chatPersistenceService.getCitationsByMessageId(messageId));
        } else if (suggestionsId != null) {
            return Result.success(chatPersistenceService.getCitationsByMessageId(suggestionsId));
        } else {
            return Result.error("messageId 或 suggestionsId 必须提供");
        }
    }
}
