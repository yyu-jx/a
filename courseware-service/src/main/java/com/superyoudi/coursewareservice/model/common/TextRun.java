package com.superyoudi.coursewareservice.model.common;

import com.superyoudi.coursewareservice.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文本运行
 * <p>
 * 文本段的子元素，用于单独控制某一小段文本及其样式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextRun {

    /// 文本内容
    private String text;

    /// 字体大小 px
    private Double fontSize;

    /// 字体
    private String fontFamily;

    /// 字体颜色 #RRGGBB
    private String fontColor;

    /// 是否加粗
    private Boolean bold;

    /// 是否斜体
    private Boolean italic;

    /// 是否有下划线
    private Boolean underline;

    /// 字符间距 px
    private Double letterSpacing;

    @SuppressWarnings("unchecked")
    public static List<TextRun> fromObjectMapList(Map<String, Object> map) {
        List<Object> textRuns = (List<Object>) map.get("textRuns");
        return textRuns.stream().map(textRun -> {
            Map<String, Object> textRunMap = (Map<String, Object>) textRun;
            return new TextRun(
                    (String) textRunMap.get("text"),
                    CommonUtils.objectToDouble(textRunMap.get("fontSize")),
                    (String) textRunMap.get("fontFamily"),
                    (String) textRunMap.get("fontColor"),
                    (Boolean) textRunMap.get("bold"),
                    (Boolean) textRunMap.get("italic"),
                    (Boolean) textRunMap.get("underline"),
                    CommonUtils.objectToDouble(textRunMap.get("letterSpacing"))
            );
        }).toList();
    }
}
