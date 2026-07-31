package com.superyoudi.coursewareservice.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

import static com.superyoudi.coursewareservice.utils.CommonUtils.objectToDouble;

/**
 * 文本段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextParagraph {

    /// 文本运行列表
    private List<TextRun> textRuns;

    /// 行高 (倍数)
    private Double lineHeight;

    /// 文本缩进
    private Double textIndent;

    /// 'left' | 'center' | 'right' | null
    @Nullable
    private String textAlign;


    public static TextParagraph fromObjectMap(Map<String, Object> map) {
        List<TextRun> textRunList = TextRun.fromObjectMapList(map);

        return new TextParagraph(
                textRunList,
                objectToDouble(map.get("lineHeight")),
                objectToDouble(map.get("textIndent")),
                (String) map.get("textAlign")
        );

    }
}
