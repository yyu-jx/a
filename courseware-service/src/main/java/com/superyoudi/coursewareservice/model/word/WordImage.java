package com.superyoudi.coursewareservice.model.word;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.superyoudi.coursewareservice.utils.CommonUtils.objectToDouble;

/**
 * Word的图片元素，可作为图片、图表和公式元素。附带一些段落样式。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordImage {

    /**
     * 图片URL
     */
    private String imgUrl;

    /**
     * 图片宽度 px
     */
    private Double width;

    /**
     * 图片高度 px
     */
    private Double height;

    /**
     * 图片段前间距 px
     */
    private Double spacingBefore;

    /**
     * 图片段后间距 px
     */
    private Double spacingAfter;

    /**
     * 图片对齐方式
     * <p>
     * left, center, right, justify
     */
    private String align;

    public static WordImage fromObjectMap(Map<String, Object> map) {

        return new WordImage(
                (String) map.get("imgUrl"),
                objectToDouble(map.get("width")),
                objectToDouble(map.get("height")),
                objectToDouble(map.get("spacingBefore")),
                objectToDouble(map.get("spacingAfter")),
                (String) map.get("align")
        );
    }
}
