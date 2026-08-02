package com.superyoudi.coursewareservice.utils.word;

import com.superyoudi.coursewareservice.model.common.BorderStyle;
import com.superyoudi.coursewareservice.model.common.TableCell;
import com.superyoudi.coursewareservice.model.common.TableData;
import com.superyoudi.coursewareservice.model.common.TextRun;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;
import java.util.List;

import static com.superyoudi.coursewareservice.utils.CommonUtils.createTableCells;


public class WordTableUtils {

    /**
     * 将PPT表格操作转换为Word文档操作
     */
    public static void createTableElement(TableData tableData, XWPFDocument document) {
        // 创建表格
        int rowCount = tableData.getRowCount();
        int colCount = tableData.getColCount();

        XWPFTable table = document.createTable(rowCount, colCount);

        // 3. 设置行高
        // setupTableRowHeights(table, tableData);

        // 4. 设置列宽
        setupTableColumnWidths(table, tableData);

        // 5. 创建表格数据矩阵
        TableCell[][] tableCells = createTableCells(tableData);

        // 6. 标记已处理的单元格
        boolean[][] visited = new boolean[rowCount][colCount];

        // 7. 遍历并填充单元格内容
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (visited[r][c]) continue;

                TableCell cellData = tableCells[r][c];
                if (cellData == null) continue;

                // 安全检查：确保行和单元格存在
                XWPFTableRow row = table.getRow(r);
                if (row == null) continue;

                // 注意：合并后某些单元格可能已被删除，需要检查索引
                if (c >= row.getTableCells().size()) continue;

                XWPFTableCell tableCell = row.getCell(c);
                if (tableCell == null) continue; // 添加空值检查

                // 应用单元格样式和内容
                applyTableCellDataToWord(cellData, tableCell);

                // 处理单元格合并
                int rowSpan = cellData.getRowSpan();
                int colSpan = cellData.getColSpan();

                if (rowSpan > 1 || colSpan > 1) {
                    int endRow = Math.min(r + rowSpan - 1, rowCount - 1);
                    int endCol = Math.min(c + colSpan - 1, colCount - 1);

                    // Word中的单元格合并
                    mergeCells(table, r, endRow, c, endCol);

                    // 标记已合并的区域
                    for (int rr = r; rr <= endRow; rr++) {
                        for (int cc = c; cc <= endCol; cc++) {
                            visited[rr][cc] = true;
                        }
                    }
                } else {
                    visited[r][c] = true;
                }
            }
        }
    }

    /**
     * 移除表格单元格内段落的额外间距，并设置标准行距。
     * 此方法通常用于确保表格内的文本布局紧凑且一致，避免默认样式带来的多余空白。
     *
     * @param paragraph 需要调整间距的 XWPFParagraph 对象（通常位于表格单元格内）
     */
    private static void removeTableCellSpacing(XWPFParagraph paragraph) {
        CTPPr pPr = paragraph.getCTP().getPPr();
        if (pPr == null) {
            pPr = paragraph.getCTP().addNewPPr();
        }

        CTSpacing spacing = pPr.isSetSpacing() ? pPr.getSpacing() : pPr.addNewSpacing();

        // 设置段前段后间距为 0
        spacing.setBefore(BigInteger.ZERO);
        spacing.setAfter(BigInteger.ZERO);
        spacing.setLine(BigInteger.valueOf(240)); // 设置行距为单倍行距（20磅 * 240 = 单倍）
        spacing.setLineRule(STLineSpacingRule.AUTO);
    }

    /**
     * 设置表格行高
     * 将PPT中的像素值转换为Word中的DXA单位
     */
    private static void setupTableRowHeights(XWPFTable table, TableData tableData) {
        List<Double> rowHeights = tableData.getRowHeights();

        for (int i = 0; i < table.getNumberOfRows() && i < rowHeights.size(); i++) {
            XWPFTableRow row = table.getRow(i);
            Double heightInPx = rowHeights.get(i);

            if (heightInPx != null) {
                // 将像素转换为DXA（1像素 ≈ 15 DXA，72/96 用于像素到点的转换）
                int heightInDXA = (int) (heightInPx * 15 * 0.8);
                row.setHeight(heightInDXA);
            }
        }
    }

    /**
     * 设置表格列宽
     */
    private static void setupTableColumnWidths(XWPFTable table, TableData tableData) {
        List<Double> colWidths = tableData.getColWidths();
        int numberOfCols = table.getRows().get(0).getTableCells().size();

        for (int i = 0; i < numberOfCols && i < colWidths.size(); i++) {
            Double widthInPx = colWidths.get(i);

            if (widthInPx != null) {
                // 将像素转换为DXA
                int widthInDXA = (int) (widthInPx * 15);

                // 为每一行的该列设置宽度
                for (XWPFTableRow row : table.getRows()) {
                    if (row.getCell(i) != null) {
                        row.getCell(i).setWidth(String.valueOf(widthInDXA));
                    }
                }
            }
        }
    }

    /**
     * Word中的单元格合并操作
     * 对应PPT中的 table.mergeCells(r, endRow, c, endCol)
     */
    private static void mergeCells(XWPFTable table, int startRow, int endRow, int startCol, int endCol) {
        // 遍历指定范围内的所有单元格
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            XWPFTableRow row = table.getRow(rowIndex);
            if (row == null) continue;

            for (int cellIndex = startCol; cellIndex <= endCol; cellIndex++) {
                XWPFTableCell cell = row.getCell(cellIndex);
                if (cell == null) continue;

                CTTc ctTc = cell.getCTTc();

                // 创建或获取水平合并节点
                CTTcPr tcPr = ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();

                // 创建合并标记节点
                if (rowIndex == startRow && cellIndex == startCol) {
                    // 左上角单元格：既是水平合并的起点，也是垂直合并的起点
                    // 设置水平合并为 "restart"
                    CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();
                    hMerge.setVal(STMerge.RESTART);

                    // 设置垂直合并为 "restart"
                    CTVMerge vMerge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
                    vMerge.setVal(STMerge.RESTART);
                } else if (rowIndex == startRow) {
                    // 第一行的其他单元格：只需水平合并
                    CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();
                    hMerge.setVal(STMerge.CONTINUE);

                    // 如果是新列，还需要设置垂直合并的起点
                    if (cellIndex > startCol) {
                        CTVMerge vMerge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
                        vMerge.setVal(STMerge.RESTART);
                    }
                } else if (cellIndex == startCol) {
                    // 第一列的其他行：只需垂直合并
                    CTVMerge vMerge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
                    vMerge.setVal(STMerge.CONTINUE);

                    // 如果是新行，还需要设置水平合并的起点
                    if (rowIndex > startRow) {
                        CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();
                        hMerge.setVal(STMerge.RESTART);
                    }
                } else {
                    // 其他单元格：水平和垂直都继续合并
                    if (!tcPr.isSetHMerge()) {
                        CTHMerge hMerge = tcPr.addNewHMerge();
                        hMerge.setVal(STMerge.CONTINUE);
                    }

                    if (!tcPr.isSetVMerge()) {
                        CTVMerge vMerge = tcPr.addNewVMerge();
                        vMerge.setVal(STMerge.CONTINUE);
                    }
                }
            }
        }
    }

    /**
     * 将单元格数据应用到Word表格单元格
     */
    private static void applyTableCellDataToWord(TableCell cellData, XWPFTableCell tableCell) {

        // 1. 设置文本内容
        if (cellData.getContent() == null)
            return;

        // 清空默认的段落内容
        for (int i = tableCell.getParagraphs().size(); i > 0; i--) {
            tableCell.removeParagraph(0);
        }

        XWPFParagraph paragraph = tableCell.addParagraph();

        for (TextRun textRun : cellData.getContent().getTextRuns()) {
            XWPFRun run = paragraph.createRun();

            String text = textRun.getText();

            // 换行
            if (text.equals("\n")) {
                run.addBreak();
                continue;
            }

            run.setText(text);

            run.setFontSize(textRun.getFontSize() * 72.0 / 96.0);
            run.setFontFamily(textRun.getFontFamily());
            run.setColor(textRun.getFontColor().substring(1));
            run.setBold(textRun.getBold());
            run.setItalic(textRun.getItalic());
            run.setUnderline(textRun.getUnderline() ? UnderlinePatterns.SINGLE : UnderlinePatterns.NONE);

            if (textRun.getLetterSpacing() != null) {
                run.setCharacterSpacing(textRun.getLetterSpacing().intValue() * 15);
            }

        }

        // 设置水平对齐
        if (cellData.getContent().getTextAlign() != null) {
            switch (cellData.getContent().getTextAlign()) {
                case "center" -> paragraph.setAlignment(ParagraphAlignment.CENTER);
                case "right" -> paragraph.setAlignment(ParagraphAlignment.RIGHT);
                default -> paragraph.setAlignment(ParagraphAlignment.LEFT);
            }
        }


        // 2. 设置背景色
        if (cellData.getBackgroundColor() != null) {
            setCellBackgroundColorInWord(tableCell, cellData.getBackgroundColor().substring(1));
        }

        // 3. 设置垂直对齐
        if (cellData.getVerticalAlign() != null) {
            switch (cellData.getVerticalAlign().toLowerCase()) {
                case "top" -> tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.TOP);
                case "bottom" -> tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.BOTTOM);
                default -> tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            }
        }

        // 4. 设置边框
        if (cellData.getBorders() != null) {
            setCellBordersInWord(tableCell, cellData.getBorders());
        }

        // 5. 设置内边距
        if (cellData.getPaddings() != null) {
            setCellPaddingsInWord(tableCell, cellData.getPaddings());
        }

        removeTableCellSpacing(paragraph);
    }

    /**
     * 设置单元格背景色
     */
    private static void setCellBackgroundColorInWord(XWPFTableCell cell, String color) {
        CTTc cttc = cell.getCTTc();
        CTTcPr tcPr = cttc.isSetTcPr() ? cttc.getTcPr() : cttc.addNewTcPr();

        CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setFill(color);
        shd.setVal(STShd.CLEAR);
    }

    /**
     * 设置单元格边框
     */
    private static void setCellBordersInWord(XWPFTableCell cell, BorderStyle[] borders) {
        CTTcPr tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null) {
            tcPr = cell.getCTTc().addNewTcPr();
        }

        CTTcBorders tcBorders = tcPr.isSetTcBorders() ? tcPr.getTcBorders() : tcPr.addNewTcBorders();

        // 上边框
        if (borders[0] != null) {
            CTBorder top = tcBorders.isSetTop() ? tcBorders.getTop() : tcBorders.addNewTop();
            applyBorderStyle(top, borders[0]);
        }

        // 右边框
        if (borders[1] != null) {
            CTBorder right = tcBorders.isSetRight() ? tcBorders.getRight() : tcBorders.addNewRight();
            applyBorderStyle(right, borders[1]);
        }

        // 下边框
        if (borders[2] != null) {
            CTBorder bottom = tcBorders.isSetBottom() ? tcBorders.getBottom() : tcBorders.addNewBottom();
            applyBorderStyle(bottom, borders[2]);
        }

        // 左边框
        if (borders[3] != null) {
            CTBorder left = tcBorders.isSetLeft() ? tcBorders.getLeft() : tcBorders.addNewLeft();
            applyBorderStyle(left, borders[3]);
        }
    }

    /**
     * 应用边框样式到指定的边框对象
     *
     * @param border CTBorder对象
     * @param style  BorderStyle样式配置
     */
    private static void applyBorderStyle(CTBorder border, BorderStyle style) {
        // 1. 设置边框样式（style）
        if (style.getStyle() != null) {
            STBorder.Enum borderType = convertToSTBorder(style.getStyle());
            if (borderType != null) {
                border.setVal(borderType);
            }
        }

        // 2. 设置边框宽度（width）
        // 单位转换：将点（point）转换为 DXAZD （1/8 点）
        // 公式：sz = width * 8
        if (style.getWidth() != null && style.getWidth() > 0) {
            int sz = (int) (style.getWidth() * 8);
            border.setSz(BigInteger.valueOf(sz));
        }

        // 3. 设置边框颜色（color）
        if (style.getColor() != null && !style.getColor().isEmpty()) {
            // 移除可能存在的 # 号前缀
            String colorHex = style.getColor().startsWith("#")
                    ? style.getColor().substring(1)
                    : style.getColor();
            border.setColor(colorHex);
        }
    }

    /**
     * 将 BorderStyleType 枚举转换为 POI 的 STBorder 枚举
     *
     * @param styleType 自定义的边框样式类型
     * @return POI 的 STBorder 枚举值
     */
    private static STBorder.Enum convertToSTBorder(BorderStyle.StyleType styleType) {
        if (styleType == null) {
            return STBorder.SINGLE; // 默认实线
        }

        return switch (styleType) {
            case DASHED -> STBorder.DASHED;
            case DOTTED -> STBorder.DOTTED;
            default -> STBorder.SINGLE;
        };
    }

    /**
     * 设置单元格内边距
     *
     * @param cell     单元格对象
     * @param paddings [上, 右, 下, 左] 单位：像素
     */
    private static void setCellPaddingsInWord(XWPFTableCell cell, Double[] paddings) {
        CTTcPr tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null) {
            tcPr = cell.getCTTc().addNewTcPr();
        }

        CTTcMar tcMar = tcPr.isSetTcMar() ? tcPr.getTcMar() : tcPr.addNewTcMar();

        // 上边距
        if (paddings[0] != null) {
            CTTblWidth top = tcMar.isSetTop() ? tcMar.getTop() : tcMar.addNewTop();
            top.setW(BigInteger.valueOf((long) (paddings[0] * 15)));
            top.setType(STTblWidth.DXA);  // 设置单位为 DXA
        }

        // 右边距
        if (paddings[1] != null) {
            CTTblWidth right = tcMar.isSetRight() ? tcMar.getRight() : tcMar.addNewRight();
            right.setW(BigInteger.valueOf((long) (paddings[1] * 15)));
            right.setType(STTblWidth.DXA);
        }

        // 下边距
        if (paddings[2] != null) {
            CTTblWidth bottom = tcMar.isSetBottom() ? tcMar.getBottom() : tcMar.addNewBottom();
            bottom.setW(BigInteger.valueOf((long) (paddings[2] * 15)));
            bottom.setType(STTblWidth.DXA);
        }

        // 左边距
        if (paddings[3] != null) {
            CTTblWidth left = tcMar.isSetLeft() ? tcMar.getLeft() : tcMar.addNewLeft();
            left.setW(BigInteger.valueOf((long) (paddings[3] * 15)));
            left.setType(STTblWidth.DXA);
        }
    }

}
