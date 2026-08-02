package com.superyoudi.coursewareservice.model.ppt;

import com.superyoudi.coursewareservice.model.common.ElementType;
import com.superyoudi.coursewareservice.model.common.TableData;
import com.superyoudi.coursewareservice.model.common.TextParagraph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/// 幻灯片元素
///
/// 根据elemType来转换元素：
/// - TEXT - TextParagraph
/// - IMAGE - URL
/// - TABLE - TableData
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlideElement {
    private ElementType elemType;
    private ElementRect rect;
    private Object elem;

    @SuppressWarnings("unchecked")
    public TextParagraph asTextParagraph() {
        return TextParagraph.fromObjectMap((Map<String, Object>) elem);
    }

    @SuppressWarnings("unchecked")
    public TableData asTableData() {
        Map<String, Object> map = (Map<String, Object>) elem;
        return TableData.fromObjectMap(map);
    }

    @SuppressWarnings("unchecked")
    public VideoData asVideoData() {
        Map<String, Object> map = (Map<String, Object>) elem;
        return VideoData.fromObjectMap(map);
    }

    public String asImageUrl() {
        return (String) elem;
    }

}
