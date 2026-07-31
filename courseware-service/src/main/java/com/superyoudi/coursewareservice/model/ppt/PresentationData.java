package com.superyoudi.coursewareservice.model.ppt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresentationData {
    private String pptName;
    private List<SlideData> slides;
}
