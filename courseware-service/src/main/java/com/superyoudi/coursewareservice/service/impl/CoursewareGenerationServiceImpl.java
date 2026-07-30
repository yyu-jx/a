package com.superyoudi.coursewareservice.service.impl;

import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import com.superyoudi.coursewareservice.websocket.dto.GenerationMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
// TODO
@Service
public class CoursewareGenerationServiceImpl implements CoursewareGenerationService {
    @Override
    public String getStoragePath() {
        return "";
    }

    @Override
    public List<Document> searchUserDocuments(Integer userId, String query) {
        return List.of();
    }

    @Override
    public List<Document> searchGlobalDocuments(String query) {
        return List.of();
    }

    @Override
    public String generateImageUrl(String prompt, int width, int height) {
        return "";
    }

    @Override
    public void packageFileAndNotify(Integer userId, Map<String, String> originalFiles, String zipName) {

    }

    @Override
    public void notifyFileGeneratingStatus(GenerationMessage message) {

    }
}
