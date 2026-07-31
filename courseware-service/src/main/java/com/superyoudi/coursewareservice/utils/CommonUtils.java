package com.superyoudi.coursewareservice.utils;

import com.superyoudi.coursewareservice.model.common.TableCell;
import com.superyoudi.coursewareservice.model.common.TableData;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;


@Slf4j
public class CommonUtils {

    /**
     * 将一个从HTML遍历出的线性表格列表转换为包含合并的二维单元格数组。
     * <p>
     * 通过ColSpan和RowSpan创建合并单元格，对于被合并的区域，所有逻辑单元格将指向同一个单元格的引用。
     *
     * @param tableData
     * @return
     */
    public static TableCell[][] createTableCells(TableData tableData) {
        int rowCount = tableData.getRowCount();
        int colCount = tableData.getColCount();

        TableCell[][] cells = new TableCell[rowCount][colCount];

        int cellIndex = 0;
        List<TableCell> cellList = tableData.getCells();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {

                // 跳过被合并的地方
                if (cells[r][c] != null) continue;

                if (cellIndex >= cellList.size()) break;

                TableCell cellInData = cellList.get(cellIndex);
                cells[r][c] = cellInData;

                // 填充合并单元格覆盖的区域
                int rowSpan = cellInData.getRowSpan();
                int colSpan = cellInData.getColSpan();
                for (int rr = r; rr < r + rowSpan && rr < rowCount; rr++) {
                    for (int cc = c; cc < c + colSpan && cc < colCount; cc++) {
                        cells[rr][cc] = cellInData;
                    }
                }

                cellIndex++; // 自增
            }
        }

        log.info("cells: {}", (Object) cells);
        return cells;
    }

    /**
     * 将对象转换为Double
     *
     * @param value Double对象
     * @return Double对象
     * @throws IllegalArgumentException 对象无法转换为Double
     */
    public static Double objectToDouble(Object value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Expected a Number but got: " + value.getClass().getName());
    }

    /**
     * 将 Base64 字符串转换为 InputStream
     *
     * @param base64 Base64 字符串
     * @return InputStream
     * @throws Exception 解码失败
     */
    public static InputStream base64ToInputStream(String base64) throws Exception {

        // 1. 去掉前缀，只保留纯 Base64 数据
        String base64Data = base64.substring(base64.indexOf(",") + 1);

        if (base64Data.isBlank())
            throw new Exception("base64Data is blank");

        // 2. 解码为字节数组
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // 3. 转换为 InputStream
        return new ByteArrayInputStream(imageBytes);
    }

}
