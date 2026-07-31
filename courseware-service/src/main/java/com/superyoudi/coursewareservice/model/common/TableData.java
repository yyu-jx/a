package com.superyoudi.coursewareservice.model.common;

import com.superyoudi.coursewareservice.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.superyoudi.coursewareservice.utils.CommonUtils.objectToDouble;

/**
 * 表格数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableData {
    private List<TableCell> cells;
    private Integer rowCount;
    private Integer colCount;

    /// 行高，px
    private List<Double> rowHeights;
    /// 列宽，px
    private List<Double> colWidths;

    @SuppressWarnings("unchecked")
    public static TableData fromObjectMap(Map<String, Object> map) {
        Integer rowCount = (Integer) map.get("rowCount");
        Integer colCount = (Integer) map.get("colCount");

        List<Object> rowHeightsRaw = (List<Object>) map.get("rowHeights");
        List<Double> rowHeights = rowHeightsRaw.stream().map(CommonUtils::objectToDouble).toList();

        List<Object> colWidthsRaw = (List<Object>) map.get("colWidths");
        List<Double> colWidths = colWidthsRaw.stream().map(CommonUtils::objectToDouble).toList();

        List<Object> cellsRaw = (List<Object>) map.get("cells");
        List<TableCell> cells = cellsRaw.stream().map(cellObj -> {
            Map<String, Object> cellMap = (Map<String, Object>) cellObj;

            // 转换 content 为 TextParagraph
            Map<String, Object> contentMap = (Map<String, Object>) cellMap.get("content");
            TextParagraph content = TextParagraph.fromObjectMap(contentMap);

            // 转换 borders
            List<Object> bordersRaw = (List<Object>) cellMap.get("borders");
            BorderStyle[] borders = null;
            if (bordersRaw != null) {
                borders = bordersRaw.stream().map(borderObj -> {
                    if (borderObj == null) return null; // 为空时不转换

                    Map<String, Object> borderMap = (Map<String, Object>) borderObj;
                    return new BorderStyle(
                            objectToDouble(borderMap.get("width")),
                            (String) borderMap.get("color"),
                            BorderStyle.StyleType.fromString((String) borderMap.get("style"))
                    );
                }).toArray(BorderStyle[]::new);
            }

            // 转换 paddings
            List<Object> paddingsRaw = (List<Object>) cellMap.get("paddings");
            Double[] paddings = null;
            if (paddingsRaw != null) {
                paddings = paddingsRaw.stream()
                        .map(padding -> {
                            if (padding == null) return null; // 为空时不转换
                            return objectToDouble(padding);
                        })
                        .toArray(Double[]::new);
            }

            return new TableCell(
                    content,
                    (Integer) cellMap.get("rowSpan"),
                    (Integer) cellMap.get("colSpan"),
                    borders,
                    paddings,
                    (String) cellMap.get("backgroundColor"),
                    (String) cellMap.get("verticalAlign")
            );
        }).toList();

        return new TableData(cells, rowCount, colCount, rowHeights, colWidths);
    }

}
