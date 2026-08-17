package com.superyoudi.coursewareservice.service;

import com.microsoft.playwright.Page;
import com.superyoudi.coursewareservice.pojo.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.pojo.dto.GenerationStatus;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 智能体服务
 *
 * @author Sijayyx
 */
@Service
public interface CoursewareGenerationService {

    CoursewareRecord recoverCoursewareRecord(Long id);

    String exportCourseware(CoursewareGenData coursewareGenData) throws IOException;

    String getPptHtml(Long coursewareId);

    String getWordHtml(Long coursewareId);

    Page getPptSlidePage(Long coursewareId);

    Page getDocumentPage(Long coursewareId);

    void notifyCoursewareGeneratingStatus(GenerationStatus message);

    /**
     * 搜索用户文档
     *
     * @param userId 用户ID
     * @param query  搜索关键词
     * @return 搜索结果
     */
    @Deprecated
    List<Document> searchUserDocuments(Integer userId, String query);

    /**
     * 搜索全局文档
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @Deprecated
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
