package com.superyoudi.coursewareservice.model.ppt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 幻灯片数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlideData {

    /// 幻灯片备注
    private String slideNote;
    private List<SlideElement> slideElements;
}
