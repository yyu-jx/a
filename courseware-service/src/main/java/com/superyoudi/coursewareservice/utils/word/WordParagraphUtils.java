package com.superyoudi.coursewareservice.utils.word;

import com.superyoudi.coursewareservice.model.common.TextParagraph;
import com.superyoudi.coursewareservice.model.common.TextRun;
import com.superyoudi.coursewareservice.model.word.WordParagraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WordParagraphUtils {

    private static final Map<String, Map<String, BigInteger>> listGroupMap = new HashMap<>();

    /**
     * 将 WordParagraph 对象中的文本内容、样式及段落属性应用到 XWPFParagraph 中。
     * 该方法负责处理文本对齐、字体样式（大小、颜色、加粗、斜体、下划线、字间距）、
     * 段落间距（行距、段前、段后）、缩进以及大纲级别等属性的设置。
     * 如果段落包含列表信息，还会调用 applyParagraphListStyle 应用列表样式。
     *
     * @param wordParagraph 源数据对象，包含段落的所有属性和文本运行信息
     * @param docNumbering  文档编号管理器，用于处理列表编号样式
     */
    public static void createWordParagraph(WordParagraph wordParagraph, XWPFDocument document, XWPFNumbering docNumbering) {
        XWPFParagraph xwpfParagraph = document.createParagraph();

        TextParagraph textParagraph = wordParagraph.getTextParagraph();

        // 列表层级与编号
        if (wordParagraph.getListLevel() != null && wordParagraph.getListStyle() != null)
            applyParagraphListStyle(xwpfParagraph, wordParagraph, docNumbering);

        // 文本对齐
        String textAlign = textParagraph.getTextAlign();
        if (textAlign != null) switch (textAlign) {
            case "center" -> xwpfParagraph.setAlignment(ParagraphAlignment.CENTER);
            case "right" -> xwpfParagraph.setAlignment(ParagraphAlignment.RIGHT);
            case "justify" -> xwpfParagraph.setAlignment(ParagraphAlignment.BOTH);
            default -> xwpfParagraph.setAlignment(ParagraphAlignment.LEFT);
        }

        // 处理文本运行
        for (TextRun textRun : textParagraph.getTextRuns()) {
            String text = textRun.getText();
            if (text.equals("\n")) {
                // XWPF 中添加换行符
                XWPFRun run = xwpfParagraph.createRun();
                run.addBreak();
                continue;
            }

            XWPFRun xwpfRun = xwpfParagraph.createRun();
            log.info("text: {}", text);
            xwpfRun.setText(text.trim());

            if (textRun.getFontSize() != null) xwpfRun.setFontSize(textRun.getFontSize().intValue() * 72.0 / 96.0);

            // 字体颜色
            if (textRun.getFontColor() != null) xwpfRun.setColor(textRun.getFontColor().substring(1));

            // 字体样式
            xwpfRun.setBold(textRun.getBold());
            xwpfRun.setItalic(textRun.getItalic());

            // 下划线
            if (textRun.getUnderline() != null && textRun.getUnderline())
                xwpfRun.setUnderline(UnderlinePatterns.SINGLE);

            // 字体
            if (textRun.getFontFamily() != null) xwpfRun.setFontFamily(textRun.getFontFamily());

            Double letterSpacing = textRun.getLetterSpacing();
            if (letterSpacing != null)
                xwpfRun.setCharacterSpacing(letterSpacing.intValue() * 15);

        }

        CTPPr pPr = xwpfParagraph.getCTP().isSetPPr() ? xwpfParagraph.getCTP().getPPr() : xwpfParagraph.getCTP().addNewPPr();
        CTSpacing spacing = pPr.isSetSpacing() ? pPr.getSpacing() : pPr.addNewSpacing();

        // 设置行间距
        if (textParagraph.getLineHeight() != null) {
            spacing.setLine(BigInteger.valueOf(Math.round(textParagraph.getLineHeight() * 1 * 240))); // 1 行 = 240 twips
            spacing.setLineRule(STLineSpacingRule.EXACT);
        }

        if (!textParagraph.getTextRuns().isEmpty()) {
            TextRun refTextRun = textParagraph.getTextRuns().get(0);
            // 乘0.7，增加段前的一点空间
            spacing.setBefore(BigInteger.valueOf((Math.round(wordParagraph.getSpacingBefore()
                    + refTextRun.getFontSize() * 0.7) * 15)));
            spacing.setAfter(BigInteger.valueOf((Math.round(wordParagraph.getSpacingAfter()) * 15)));
        }

        xwpfParagraph.setIndentationFirstLine(textParagraph.getTextIndent().intValue() * 15);

        // 左缩进
        xwpfParagraph.setIndentationLeft(wordParagraph.getLeftIndent().intValue() * 15);
        log.info("left: {}", wordParagraph.getLeftIndent());

        if (wordParagraph.getOutlineLevel() != null)
            applyParagraphOutlineLevel(xwpfParagraph, wordParagraph.getOutlineLevel());
    }

    /**
     * 设置段落的大纲级别（Outline Level）。
     * 大纲级别用于定义文档的结构层次，通常对应 Word 中的标题样式层级。
     *
     * @param paragraph 需要设置大纲级别的 XWPFParagraph 对象
     * @param level     大纲级别，范围通常为 0-8。0 表示最高层级（如标题 1），数值越大层级越低
     */
    public static void applyParagraphOutlineLevel(XWPFParagraph paragraph, int level) {
        // 获取或创建段落属性
        CTPPr pPr = paragraph.getCTP().isSetPPr() ?
                paragraph.getCTP().getPPr() :
                paragraph.getCTP().addNewPPr();

        // 设置大纲等级
        if (pPr.isSetOutlineLvl()) pPr.getOutlineLvl().setVal(BigInteger.valueOf(level));
        else {
            CTDecimalNumber outlineLvl = pPr.addNewOutlineLvl();
            outlineLvl.setVal(BigInteger.valueOf(level));
        }
    }

    public static void clearListCache() {
        listGroupMap.clear();
    }

    /**
     * 将文本运行（TextRun）的样式应用到编号列表的层级样式（CTLvl）中。
     * 这确保了列表项的编号文本具有与参考文本一致的字体、大小、颜色等外观特征。
     *
     * @param lvl             需要应用样式的 CTLvl 对象，代表列表的一个层级
     * @param refTextRunStyle 参考的 TextRun 样式对象，从中提取字体、大小、颜色等信息
     */
    private static void applyNumberingStyle(CTLvl lvl, TextRun refTextRunStyle) {

        if (refTextRunStyle == null) return;

        CTRPr rPr = lvl.isSetRPr() ? lvl.getRPr() : lvl.addNewRPr();

        if (refTextRunStyle.getFontSize() != null) {
            CTHpsMeasure sz = rPr.addNewSz();
            sz.setVal(BigInteger.valueOf(Math.round(refTextRunStyle.getFontSize() * 1.5)));
        }

        if (refTextRunStyle.getFontFamily() != null) {
            CTFonts fonts = rPr.addNewRFonts();

            fonts.setAscii(refTextRunStyle.getFontFamily());
            fonts.setHAnsi(refTextRunStyle.getFontFamily());

        }
        // 设置字体颜色
        if (refTextRunStyle.getFontColor() != null) {
            CTColor color = rPr.addNewColor();
            color.setVal(refTextRunStyle.getFontColor().substring(1));
        }
        // 设置加粗
        if (refTextRunStyle.getBold()) rPr.addNewB();

        // 设置斜体
        if (refTextRunStyle.getItalic()) rPr.addNewI();

        // 设置下划线
        if (refTextRunStyle.getUnderline()) {
            CTUnderline u = rPr.addNewU();
            u.setVal(STUnderline.SINGLE);
        }
    }

    /**
     * 创建编号样式
     */
    private static BigInteger createNumberingStyle(TextRun refTextRunStyle, String listStyle, Integer listLevel, XWPFNumbering numbering) {
        // 创建抽象编号
        CTAbstractNum cTAbstractNum = CTAbstractNum.Factory.newInstance();
        // 使用全局唯一ID（基于已创建的抽象编号数量）
        BigInteger abstractNumId = BigInteger.valueOf(listGroupMap.size());
        cTAbstractNum.setAbstractNumId(abstractNumId);

        // 配置指定层级
        CTLvl cTLvl = cTAbstractNum.addNewLvl();
        cTLvl.setIlvl(BigInteger.valueOf(listLevel));

        // 设置编号格式
        setupNumberingFormat(cTLvl, listStyle);

        // 应用样式
        applyNumberingStyle(cTLvl, refTextRunStyle);

        // 设置起始编号为1
        cTLvl.addNewStart().setVal(BigInteger.valueOf(1));

        // 注册抽象编号
        XWPFAbstractNum abstractNum = new XWPFAbstractNum(cTAbstractNum);
        BigInteger finalAbstractNumId = numbering.addAbstractNum(abstractNum);

        // 创建编号实例并返回
        return numbering.addNum(finalAbstractNumId);
    }

    /**
     * 根据 listStyle 配置编号格式
     */
    private static void setupNumberingFormat(CTLvl cTLvl, String listStyle) {
        switch (listStyle) {
            case "disc":  // 实心圆
                cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
                cTLvl.addNewLvlText().setVal("•"); // u+2219
                break;
            case "circle":  // 空心圆
                cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
                cTLvl.addNewLvlText().setVal("◦"); // u+25E6
                break;
            case "square":  // 实心方块
                cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
                cTLvl.addNewLvlText().setVal("▪"); // u+25AA
                break;
            case "decimal":  // 1, 2, 3...
            case "decimal-leading-zero":  // 01, 02, 03...
                cTLvl.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
                cTLvl.addNewLvlText().setVal("%1.");
                break;
            case "lower-alpha":
            case "lower-latin":  // a, b, c...
                cTLvl.addNewNumFmt().setVal(STNumberFormat.LOWER_LETTER);
                cTLvl.addNewLvlText().setVal("%1.");
                break;
            case "upper-alpha":
            case "upper-latin":  // A, B, C...
                cTLvl.addNewNumFmt().setVal(STNumberFormat.UPPER_LETTER);
                cTLvl.addNewLvlText().setVal("%1.");
                break;
            case "lower-roman":  // i, ii, iii...
                cTLvl.addNewNumFmt().setVal(STNumberFormat.LOWER_ROMAN);
                cTLvl.addNewLvlText().setVal("%1.");
                break;
            case "upper-roman":  // I, II, III...
                cTLvl.addNewNumFmt().setVal(STNumberFormat.UPPER_ROMAN);
                cTLvl.addNewLvlText().setVal("%1.");
                break;
            case "none":  // 无标记
                cTLvl.addNewNumFmt().setVal(STNumberFormat.NONE);
                cTLvl.addNewLvlText().setVal("");
                break;
            default:  // 自定义字符串
                cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
                cTLvl.addNewLvlText().setVal(listStyle);
                break;
        }
    }

    /**
     * 为段落应用列表样式和编号。
     * 该方法根据列表样式、层级和 ID 管理编号定义。如果指定的样式组合尚未创建，
     * 则通过 createNumberingStyle 创建新的编号样式并缓存，以确保相同样式的列表项具有连续的编号。
     *
     * @param paragraph     需要应用列表样式的 XWPFParagraph 对象
     * @param wordParagraph 源数据对象，包含列表样式、层级和 ID 等信息
     * @param docNumbering  文档编号管理器，用于注册和获取编号定义
     */
    private static void applyParagraphListStyle(XWPFParagraph paragraph, WordParagraph wordParagraph, XWPFNumbering docNumbering) {
        String listStyle = wordParagraph.getListStyle();
        Integer listLevel = wordParagraph.getListLevel();
        String listId = wordParagraph.getListId();

        // 如果没有指定 listId，使用默认值（保证同一个文档中所有没有 listId 的段落连续编号）
        if (listId == null || listId.isEmpty()) listId = "_default_";

        // 创建样式key
        String styleKey = listStyle + "_" + listLevel;

        // 获取或创建该 listId 对应的样式映射
        Map<String, BigInteger> styleMap = listGroupMap.computeIfAbsent(listId,
                k -> new HashMap<>());

        // 获取或创建 numId
        BigInteger numId = styleMap.get(styleKey);
        if (numId == null) {


            // 获取参考样式
            TextRun refTextRun;
            if (wordParagraph.getTextParagraph().getTextRuns().isEmpty()) refTextRun = null;
            else refTextRun = wordParagraph.getTextParagraph().getTextRuns().get(0);

            numId = createNumberingStyle(refTextRun, listStyle, listLevel, docNumbering);
            styleMap.put(styleKey, numId);
        }

        // 应用到段落
        paragraph.setNumID(numId);

    }
}
