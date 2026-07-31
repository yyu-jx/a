package com.superyoudi.coursewareservice.service;

import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.websocket.dto.GenerationMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 智能体服务
 *
 * @author Sijayyx
 */
@Service
public interface CoursewareGenerationService {


    String exportCourseware(CoursewareGenData coursewareGenData) throws IOException;

//    void createPpt(PresentationData presentationData) throws IOException;
//
//    void createWord(WordData wordData);


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

    /**
     * 打包文件并通知用户
     *
     * @param userId        用户ID
     * @param originalFiles 文件映射 - <类型, 文件名>
     * @param zipName       压缩包名称
     * @author Sijayyx
     */
    void packageFileAndNotify(Integer userId, Map<String, String> originalFiles, String zipName);

    /**
     * 通知用户文件生成进度
     *
     * @param message 进度信息
     * @author Sijayyx
     */
    void notifyFileGeneratingStatus(GenerationMessage message);
}
