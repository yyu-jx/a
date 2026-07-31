package com.superyoudi.coursewareservice.service;

import com.microsoft.playwright.Page;
import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.dto.GenerationStatus;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 智能体服务
 *
 * @author Sijayyx
 */
@Service
public interface CoursewareGenerationService {

    String exportCourseware(CoursewareGenData coursewareGenData) throws IOException;

    String getPptHtml(Integer coursewareId);

    String getWordHtml(Integer coursewareId);

    Page getPptSlidePage(Integer coursewareId);

    Page getDocumentPage(Integer coursewareId);

    void notifyCoursewareGeneratingStatus(GenerationStatus message);

    /**
     * 搜索用户文件
     *
     * @param userId 用户ID
     * @param query  搜索关键词
     * @return 搜索结果
     */
    List<Document> searchUserDocuments(Integer userId, String query);

    /**
     * 搜索全局文件
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    List<Document> searchGlobalDocuments(String query);

    /**
     * 生成图片，并返回其原始URL
     *
     * @param prompt 提示词
     * @param width  图片宽度
     * @param height 图片高度
     * @return 图片原始URL
     */
    String generateImageUrl(String prompt, int width, int height);

}
