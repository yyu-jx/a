package com.superyoudi.coursewareservice.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/// 表格单元格
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableCell {
    private TextParagraph content;
    private Integer rowSpan;
    private Integer colSpan;

    // 样式
    /**
     * [上、右、下、左] , 可为null
     */
    private @Nullable BorderStyle[] borders;

    /**
     * [上、右、下、左] px
     */
    private @Nullable Double[] paddings;
    
    @Nullable
    private String backgroundColor;

    /**
     * 'top' | 'middle' | 'bottom' | null
     */
    @Nullable
    private String verticalAlign;
}
