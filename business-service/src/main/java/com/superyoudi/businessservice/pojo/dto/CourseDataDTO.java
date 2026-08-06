package com.superyoudi.businessservice.pojo.dto;

import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Sijayyx
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseDataDTO {

    private Long courseId;
    private String courseName;
    private List<ChapterData> chapterDataList;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChapterData {
        private String chapterName;
        private Integer order;
        private List<SectionData> sectionDataList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionData {
        private String sectionName;
        private Integer order;
        private List<KnowledgeExtractionResult> extractionResults;
    }


}
