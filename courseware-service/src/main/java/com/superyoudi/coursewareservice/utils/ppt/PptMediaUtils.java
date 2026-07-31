package com.superyoudi.coursewareservice.utils.ppt;

import com.superyoudi.coursewareservice.model.ppt.ElementRect;
import com.superyoudi.coursewareservice.model.ppt.VideoData;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.presentationml.x2006.main.*;

import javax.xml.namespace.QName;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import static com.superyoudi.coursewareservice.utils.CommonUtils.base64ToInputStream;

/**
 * PPT 媒体元素工具类
 */
public class PptMediaUtils {

    // TODO 考虑把它删了？
    private final static DecimalFormat TIME_FORMAT = new DecimalFormat("0.####");

    /**
     * 添加视频元素
     *
     * @param rect      元素位置
     * @param videoData 视频数据
     * @param ppt       PPTX对象
     * @param slide     幻灯片
     * @param pageSize  页面大小
     * @throws Exception 添加视频元素异常
     * @author Sijayyx
     */
    public static void addVideoElement(ElementRect rect, VideoData videoData, XMLSlideShow ppt, XSLFSlide slide, Dimension pageSize) throws Exception {
        InputStream coverImage = base64ToInputStream(videoData.getCoverUrl());
        InputStream videoIs = new FileInputStream("E:\\DevStudy\\ppt-generation-test\\src\\main\\resources\\" + videoData.getVidUrl());

        XSLFPictureData data = ppt.addPicture(coverImage, PictureData.PictureType.PNG);
        XSLFPictureShape pictureShape = slide.createPicture(data);
        pictureShape.setAnchor(rect.toRelativeRect2D(pageSize.getWidth(), pageSize.getHeight()));

        // 暂时使用vidUrl代替vidName
        insertVideoIntoSlide(ppt, slide, pictureShape, "test.mp4", videoIs, videoData.getDuration());
    }

    /**
     * 添加图片元素
     *
     * @param rect     元素位置
     * @param imageUrl 图片地址
     * @param ppt      PPTX对象
     * @param slide    幻灯片
     * @param pageSize 幻灯片大小
     * @throws IOException 添加图片元素异常
     * @author Sijayyx
     */
    public static void addImageElement(ElementRect rect, String imageUrl, XMLSlideShow ppt, XSLFSlide slide, Dimension pageSize) throws IOException {
        InputStream imgIs;
        try {
            imgIs = base64ToInputStream(imageUrl);
        } catch (Exception e) {
            return;
        }

        XSLFPictureData data = ppt.addPicture(imgIs, PictureData.PictureType.PNG);
        XSLFPictureShape pictureShape = slide.createPicture(data);

        pictureShape.setAnchor(rect.toRelativeRect2D(pageSize.getWidth(), pageSize.getHeight()));
    }

    /**
     * 插入视频到幻灯片
     *
     * @param pptx                目标PPTX
     * @param slide               目标幻灯片
     * @param previewPictureShape 视频预览图片
     * @param vidName             视频名称
     * @param videoIs             视频输入流
     * @param seconds             视频时长
     * @throws InvalidFormatException 格式异常
     * @throws IOException            输入流异常
     * @author Sijayyx
     */
    private static void insertVideoIntoSlide(
            XMLSlideShow pptx,
            XSLFSlide slide,
            XSLFPictureShape previewPictureShape,
            String vidName,
            InputStream videoIs,
            double seconds
    ) throws InvalidFormatException, IOException {
        // 将视频文件嵌入到 PPTX 包中
        // 创建视频文件在 PPTX 内部的路径名称
        PackagePartName partName = PackagingURIHelper.createPartName("/ppt/media/" + vidName);
        // 在 PPTX 包中创建一个新的部分（Part）来存储视频数据，指定 MIME 类型为 video/mpeg
        PackagePart part = pptx.getPackage().createPart(partName, "video/mpeg");
        // 获取该部分的输出流，用于写入视频二进制数据
        OutputStream partOs = part.getOutputStream();

        // 将文件输入流中的数据直接传输到 PPTX 视频部分的输出流中
        videoIs.transferTo(partOs);

        // 关闭流资源，防止内存泄漏
        videoIs.close();
        partOs.close();

        // 配置视频的关联关系、点击动作及扩展属性（如修剪时间）
        addVideo(slide, part, previewPictureShape, seconds);
        // 添加幻灯片的时序信息（Timing Info），确保视频能够自动或受控播放
        addTimingInfo(slide, previewPictureShape);
    }

    /**
     * 向幻灯片的内部添加视频
     *
     * @param slide           目标幻灯片
     * @param previewPicShape 幻灯片预览图片
     * @author Sijayyx
     */
    private static void addVideo(XSLFSlide slide, PackagePart videoPart, XSLFPictureShape previewPicShape, double seconds) {
        // add video shape
        PackagePartName partName = videoPart.getPartName();
        PackageRelationship prsEmbed1 = slide.getPackagePart().addRelationship(partName, TargetMode.INTERNAL, "http://schemas.microsoft.com/office/2007/relationships/media");
        PackageRelationship prsExec1 = slide.getPackagePart().addRelationship(partName, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/video");
        CTPicture xpic1 = (CTPicture) previewPicShape.getXmlObject();
        CTHyperlink link1 = xpic1.getNvPicPr().getCNvPr().addNewHlinkClick();
        link1.setId("");
        link1.setAction("ppaction://media");

        // add video relation
        CTApplicationNonVisualDrawingProps nvPr = xpic1.getNvPicPr().getNvPr();
        nvPr.addNewVideoFile().setLink(prsExec1.getId());
        CTExtension ext = nvPr.addNewExtLst().addNewExt();
        // see http://msdn.microsoft.com/en-us/library/dd950140(v=office.12).aspx
        ext.setUri("{DAA4B4D4-6D71-4841-9C94-3DE7FCFB9230}");
        String p14Ns = "http://schemas.microsoft.com/office/powerpoint/2010/main";
        XmlCursor cur = ext.newCursor();
        cur.toEndToken();
        cur.beginElement(new QName(p14Ns, "media", "p14"));
        cur.insertNamespace("p14", p14Ns);
        cur.insertAttributeWithValue(new QName(STRelationshipId.type.getName().getNamespaceURI(), "embed"), prsEmbed1.getId());
        cur.beginElement(new QName(p14Ns, "trim", "p14"));
        cur.insertAttributeWithValue("st", TIME_FORMAT.format(0));
        cur.close();
    }

    /**
     * 添加幻灯片的时序信息，确保视频能够自动或受控播放
     *
     * @param slide           幻灯片
     * @param previewPicShape 幻灯片预览图片
     * @author Sijayyx
     */
    private static void addTimingInfo(XSLFSlide slide, XSLFPictureShape previewPicShape) {
        // add slide timing information, so video can be controlled
        CTSlide xslide = slide.getXmlObject();
        CTTimeNodeList ctnl;
        if (!xslide.isSetTiming()) {
            CTTLCommonTimeNodeData ctn = xslide.addNewTiming().addNewTnLst().addNewPar().addNewCTn();
            ctn.setDur(STTLTimeIndefinite.INDEFINITE);
            ctn.setRestart(STTLTimeNodeRestartType.NEVER);
            ctn.setNodeType(STTLTimeNodeType.TM_ROOT);
            ctnl = ctn.addNewChildTnLst();
        } else {
            ctnl = xslide.getTiming().getTnLst().getParArray(0).getCTn().getChildTnLst();
        }

        CTTLCommonMediaNodeData cmedia = ctnl.addNewVideo().addNewCMediaNode();
        cmedia.setVol(80000);
        CTTLCommonTimeNodeData ctn = cmedia.addNewCTn();
        ctn.setFill(STTLTimeNodeFillType.HOLD);
        ctn.setDisplay(false);
        ctn.addNewStCondLst().addNewCond().setDelay(STTLTimeIndefinite.INDEFINITE);
        cmedia.addNewTgtEl().addNewSpTgt().setSpid(previewPicShape.getShapeId());
    }

}
