package com.superyoudi.coursewareservice.model.word;

import com.superyoudi.coursewareservice.model.common.TextParagraph;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.superyoudi.coursewareservice.utils.CommonUtils.objectToDouble;

/**
 * Word段落元素，是对TextParagraph的封装
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class WordParagraph {

    /// 文本段落
    private TextParagraph textParagraph;

    /// 大纲级别，用于标记TOC
    @Nullable
    private Integer outlineLevel;

    /// 段前分页
    private Boolean breakBefore;

    /// 段后分页
    private Boolean breakAfter;

    /// 段前间距 px
    private Double spacingBefore;

    /// 段后间距 px
    private Double spacingAfter;

    /// 左对齐缩进 px
    private Double leftIndent;

    /// 列表段落 ID
    @Nullable
    private String listId; // 列表ID
    /// 列表层级
    @Nullable
    private Integer listLevel;

    /// 列表装饰样式
    /// - disc	● 实心圆（默认）
    /// - circle	○ 空心圆
    /// - square	■ 实心方块
    /// - decimal	1, 2, 3...
    /// - decimal-leading-zero	01, 02, 03...
    /// - lower-alpha / lower-latin	a, b, c...
    /// - upper-alpha / upper-latin	A, B, C...
    /// - lower-roman	i, ii, iii...
    /// - upper-roman	I, II, III...
    /// - none	无标记
    /// - string	自定义字符串，如 "→"
    @Nullable
    private String listStyle;

    @SuppressWarnings("unchecked")
    public static WordParagraph fromObjectMap(Map<String, Object> map) {
        Map<String, Object> textParagraphMap = (Map<String, Object>) map.get("textParagraph");
        TextParagraph textParagraph = TextParagraph.fromObjectMap(textParagraphMap);

        return new WordParagraph(
                textParagraph,
                (Integer) map.get("outlineLevel"),
                (Boolean) map.get("breakBefore"),
                (Boolean) map.get("breakAfter"),
                objectToDouble(map.get("spacingBefore")),
                objectToDouble(map.get("spacingAfter")),
                objectToDouble(map.get("leftIndent")),
                (String) map.get("listId"),
                (Integer) map.get("listLevel"),
                (String) map.get("listStyle")
        );
    }
}
