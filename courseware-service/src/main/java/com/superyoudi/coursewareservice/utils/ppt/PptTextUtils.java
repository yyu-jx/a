package com.superyoudi.coursewareservice.utils.ppt;

import com.superyoudi.coursewareservice.model.common.TextParagraph;
import com.superyoudi.coursewareservice.model.common.TextRun;
import com.superyoudi.coursewareservice.model.ppt.ElementRect;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.*;

import java.awt.*;

/**
 * PPT文本工具类
 */
public class PptTextUtils {

    /**
     * 添加文本段落
     *
     * @param rect          文本框位置
     * @param textParagraph 文本内容
     * @param slide         PPT页
     * @param pageSize      页面大小
     */
    public static void addTextParagraph(ElementRect rect, TextParagraph textParagraph, XSLFSlide slide, Dimension pageSize) {
        XSLFTextShape textShape = slide.createTextBox();
        textShape.setAnchor(rect.toRelativeRect2D(pageSize.getWidth(), pageSize.getHeight()));
        textShape.clearText();

        // 删除默认的边距
        textShape.setLeftInset(0);
        textShape.setRightInset(0);
        textShape.setTopInset(0);
        textShape.setBottomInset(0);

        applyTextAndStyle(textParagraph, textShape.addNewTextParagraph());
    }

    /**
     * 设置幻灯片备注
     *
     * @param ppt      PPT对象
     * @param slide    幻灯片
     * @param noteText 备注内容
     */
    public static void setSlideNote(XMLSlideShow ppt, XSLFSlide slide, String noteText) {
        XSLFNotes note = ppt.getNotesSlide(slide);

        for (XSLFTextShape shape : note.getPlaceholders()) {
            if (shape.getTextType() == Placeholder.BODY) {
                shape.setText(noteText);
                break;
            }
        }
    }

    /**
     * 设置文本样式
     *
     * @param textParagraph     文本内容
     * @param xslfTextParagraph XSLF文本对象
     */
    public static void applyTextAndStyle(TextParagraph textParagraph, XSLFTextParagraph xslfTextParagraph) {
        xslfTextParagraph.setLineSpacing(textParagraph.getLineHeight() * 100.0);
        xslfTextParagraph.setIndent(textParagraph.getTextIndent() * 72.0 / 96.0);

        String textAlign = textParagraph.getTextAlign();

        if (textAlign != null) {
            switch (textAlign) {
                case "center" ->
                        xslfTextParagraph.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
                case "right" ->
                        xslfTextParagraph.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.RIGHT);
                default -> xslfTextParagraph.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.LEFT);
                // default 在上面设置了
            }
        }

        for (TextRun textRun : textParagraph.getTextRuns()) {
            String text = textRun.getText();
            if (text.equals("\n")) {
                xslfTextParagraph.addLineBreak();
                continue;
            }

            XSLFTextRun xslfTextRun = xslfTextParagraph.addNewTextRun();
            xslfTextRun.setText(text.trim());
            xslfTextRun.setFontFamily(textRun.getFontFamily());
            xslfTextRun.setFontSize(textRun.getFontSize() * 72.0 / 96.0);
            xslfTextRun.setFontColor(Color.decode(textRun.getFontColor()));
            xslfTextRun.setBold(textRun.getBold());
            xslfTextRun.setItalic(textRun.getItalic());
            xslfTextRun.setUnderlined(textRun.getUnderline());

            Double letterSpacing = textRun.getLetterSpacing();
            if (letterSpacing != null)
                xslfTextRun.setCharacterSpacing(letterSpacing * 72.0 / 96.0);
        }
    }

}
