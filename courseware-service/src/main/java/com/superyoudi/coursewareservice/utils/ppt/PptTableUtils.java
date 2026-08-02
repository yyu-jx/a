package com.superyoudi.coursewareservice.utils.ppt;

import com.superyoudi.coursewareservice.model.common.BorderStyle;
import com.superyoudi.coursewareservice.model.common.TableCell;
import com.superyoudi.coursewareservice.model.common.TableData;
import com.superyoudi.coursewareservice.model.common.TextParagraph;
import com.superyoudi.coursewareservice.model.ppt.ElementRect;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.lang.Nullable;

import java.awt.*;

import static com.superyoudi.coursewareservice.utils.CommonUtils.createTableCells;
import static com.superyoudi.coursewareservice.utils.ppt.PptTextUtils.applyTextAndStyle;

/**
 * PPT表格工具类
 */
@Slf4j
public class PptTableUtils {

    /**
     * 添加表格元素
     *
     * @param rect      元素位置
     * @param tableData 表格数据
     * @param slide     幻灯片
     * @param pageSize  页面大小
     */
    public static void addTableElement(ElementRect rect, TableData tableData, XSLFSlide slide, Dimension pageSize) {
        TableCell[][] tableCells = createTableCells(tableData);

        int rowCount = tableData.getRowCount();
        int colCount = tableData.getColCount();

        XSLFTable table = slide.createTable();

        table.setAnchor(rect.toRelativeRect2D(pageSize.getWidth(), pageSize.getHeight()));

        // 创建表格的所有行列
        for (int r = 0; r < rowCount; r++) {
            XSLFTableRow row = table.addRow();
            // 设置行高
            row.setHeight(tableData.getRowHeights().get(r) * 72.0 / 96.0);

            // 创建单元格
            for (int c = 0; c < colCount; c++)
                row.addCell();
        }

        for (int c = 0; c < colCount; c++)
            table.setColumnWidth(c, tableData.getColWidths().get(c) * 72.0 / 96.0);

        // 此坐标是否处理过
        boolean[][] visited = new boolean[rowCount][colCount];

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (visited[r][c]) continue;

                TableCell cellData = tableCells[r][c];
                if (cellData == null) continue; // 空检查

                // 合并范围
                int rowSpan = cellData.getRowSpan();
                int colSpan = cellData.getColSpan();

                XSLFTableCell tableCell = table.getRows().get(r).getCells().get(c);
                // 设置样式及内容
                applyTableCellData(cellData, tableCell);

                // 处理合并，只有rowSpan和colSpan > 1 时才合并
                if (rowSpan <= 1 && colSpan <= 1) continue;

                int endRow = r + rowSpan - 1;
                int endCol = c + colSpan - 1;
                table.mergeCells(r, endRow, c, endCol);

                for (int rr = r; rr < r + rowSpan && rr < rowCount; rr++) {
                    for (int cc = c; cc < c + colSpan && cc < colCount; cc++) {
                        visited[rr][cc] = true;
                    }
                }
            }

        }
    }

    /**
     * 设置表格边框样式
     *
     * @param tableCell   表格单元格
     * @param borderStyle 边框样式
     * @param position    边框位置
     */
    private static void applyTableCellBorder(XSLFTableCell tableCell, @Nullable BorderStyle borderStyle, org.apache.poi.sl.usermodel.TableCell.BorderEdge position) {
        if (borderStyle == null) return;

        tableCell.setBorderColor(position, Color.decode(borderStyle.getColor()));
        tableCell.setBorderWidth(position, borderStyle.getWidth() * 72.0 / 96.0);
        tableCell.setBorderDash(position, StrokeStyle.LineDash.valueOf(borderStyle.getStyle().getStyleName()));
    }

    /**
     * 设置表格单元格数据
     *
     * @param cellData  表格单元格数据
     * @param tableCell 表格单元格
     */
    private static void applyTableCellData(TableCell cellData, XSLFTableCell tableCell) {
        // 文本样式
        XSLFTextParagraph paragraph = tableCell.addNewTextParagraph();
        TextParagraph content = cellData.getContent();
        applyTextAndStyle(content, paragraph);

        String verticalAlign = cellData.getVerticalAlign();
        tableCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (verticalAlign != null) {
            switch (verticalAlign) {
                case "top" -> tableCell.setVerticalAlignment(VerticalAlignment.TOP);
                case "bottom" -> tableCell.setVerticalAlignment(VerticalAlignment.BOTTOM);
                // default 在上面设置了
            }
        }

        // 背景色
        String backgroundColor = cellData.getBackgroundColor();
        if (backgroundColor != null)
            tableCell.setFillColor(Color.decode(backgroundColor));

        // 内边距
        Double[] paddings = cellData.getPaddings();
        if (paddings != null) {
            Double paddingTop = paddings[0];
            if (paddingTop != null)
                tableCell.setTopInset(paddingTop * 72.0 / 96.0);

            Double paddingRight = paddings[1];
            if (paddingRight != null)
                tableCell.setRightInset(paddingRight * 72.0 / 96.0);

            Double paddingBottom = paddings[2];
            if (paddingBottom != null)
                tableCell.setBottomInset(paddingBottom * 72.0 / 96.0);

            Double paddingLeft = paddings[3];
            if (paddingLeft != null)
                tableCell.setLeftInset(paddingLeft * 72.0 / 96.0);
        }

        // 边框
        BorderStyle[] borders = cellData.getBorders();
        if (borders != null) {
            BorderStyle topBorder = borders[0];
            applyTableCellBorder(tableCell, topBorder, org.apache.poi.sl.usermodel.TableCell.BorderEdge.top);
            BorderStyle rightBorder = borders[1];
            applyTableCellBorder(tableCell, rightBorder, org.apache.poi.sl.usermodel.TableCell.BorderEdge.right);
            BorderStyle bottomBorder = borders[2];
            applyTableCellBorder(tableCell, bottomBorder, org.apache.poi.sl.usermodel.TableCell.BorderEdge.bottom);
            BorderStyle leftBorder = borders[3];
            applyTableCellBorder(tableCell, leftBorder, org.apache.poi.sl.usermodel.TableCell.BorderEdge.left);
        }
    }

}
