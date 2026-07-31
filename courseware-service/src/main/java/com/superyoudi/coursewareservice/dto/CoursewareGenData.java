package com.superyoudi.coursewareservice.dto;

import com.superyoudi.coursewareservice.model.ppt.PresentationData;
import com.superyoudi.coursewareservice.model.word.WordData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursewareGenData {
    private Integer userId;
    private Integer coursewareRecordId;
    private PresentationData presentationData;
    private WordData wordData;
}
