package com.superyoudi.coursewareservice.utils.word;

import com.superyoudi.coursewareservice.model.word.WordImage;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

import java.io.InputStream;
import java.math.BigInteger;

import static com.superyoudi.coursewareservice.utils.CommonUtils.base64ToInputStream;

/**
 * Word图片工具类
 */
public class WordImageUtils {

    /**
     * 添加Word图片元素
     * <p>
     * 这会在当前段落末尾创建一个Paragraph，并添加一个Run，Run中添加图片
     *
     * @param document  文档对象
     * @param wordImage 图片对象
     */
    public static void addWordImage(XWPFDocument document, WordImage wordImage) {
        XWPFParagraph xwpfParagraph = document.createParagraph();
        try {
            XWPFRun imageRun = xwpfParagraph.createRun();
            InputStream inputStream = base64ToInputStream(wordImage.getImgUrl());

            int widthEmu = Units.pixelToEMU(wordImage.getWidth().intValue());
            int heightEmu = Units.pixelToEMU(wordImage.getHeight().intValue());

            imageRun.addPicture(inputStream, PictureType.PNG, "image.png", widthEmu, heightEmu);

            CTPPr pPr = xwpfParagraph.getCTP().isSetPPr() ? xwpfParagraph.getCTP().getPPr() : xwpfParagraph.getCTP().addNewPPr();
            CTSpacing spacing = pPr.isSetSpacing() ? pPr.getSpacing() : pPr.addNewSpacing();

            spacing.setBefore(BigInteger.valueOf((Math.round(wordImage.getSpacingBefore()) * 15)));
            spacing.setAfter(BigInteger.valueOf((Math.round(wordImage.getSpacingAfter()) * 15)));

            if (wordImage.getAlign() != null) {
                switch (wordImage.getAlign()) {
                    case "left":
                        pPr.addNewJc().setVal(STJc.LEFT);
                        break;
                    case "center":
                        pPr.addNewJc().setVal(STJc.CENTER);
                        break;
                    case "right":
                        pPr.addNewJc().setVal(STJc.RIGHT);
                        break;
                    case "justify":
                        pPr.addNewJc().setVal(STJc.BOTH);
                        break;
                }
            }
        } catch (Exception e) {
            xwpfParagraph.removeRun(0);
            throw new RuntimeException(e);
        }
    }

}
