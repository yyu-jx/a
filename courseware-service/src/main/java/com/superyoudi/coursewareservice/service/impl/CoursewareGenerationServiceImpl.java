package com.superyoudi.coursewareservice.service.impl;

import com.microsoft.playwright.Page;
import com.superyoudi.coursewareservice.component.FileZipper;
import com.superyoudi.coursewareservice.component.PlaywrightManager;
import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.dto.GenerationStatus;
import com.superyoudi.coursewareservice.model.common.TableData;
import com.superyoudi.coursewareservice.model.ppt.PresentationData;
import com.superyoudi.coursewareservice.model.ppt.SlideData;
import com.superyoudi.coursewareservice.model.ppt.SlideElement;
import com.superyoudi.coursewareservice.model.word.WordData;
import com.superyoudi.coursewareservice.model.word.WordElement;
import com.superyoudi.coursewareservice.model.word.WordImage;
import com.superyoudi.coursewareservice.model.word.WordParagraph;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import com.superyoudi.coursewareservice.utils.SimpleCache;
import com.superyoudi.coursewareservice.utils.word.WordTableUtils;
import com.superyoudi.coursewareservice.websocket.CoursewareGenerationEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.superyoudi.coursewareservice.utils.ppt.PptMediaUtils.addImageElement;
import static com.superyoudi.coursewareservice.utils.ppt.PptMediaUtils.addVideoElement;
import static com.superyoudi.coursewareservice.utils.ppt.PptTableUtils.addTableElement;
import static com.superyoudi.coursewareservice.utils.ppt.PptTextUtils.addTextParagraph;
import static com.superyoudi.coursewareservice.utils.ppt.PptTextUtils.setSlideNote;
import static com.superyoudi.coursewareservice.utils.word.WordImageUtils.addWordImage;
import static com.superyoudi.coursewareservice.utils.word.WordParagraphUtils.createWordParagraph;

/**
 * @author Sijayyx
 */
// TODO
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursewareGenerationServiceImpl implements CoursewareGenerationService {

    private final SimpleCache<Integer, Page> pptSlidePageCache = new SimpleCache<>();
    private final SimpleCache<Integer, Page> documentPageCache = new SimpleCache<>();

    private final PlaywrightManager playwrightManager;

    @Value("${app.storage.exports}")
    private String exportFilePath;

    /**
     * 导出课件，返回压缩包文件名
     *
     * @return 压缩包文件名
     */
    public String exportCourseware(CoursewareGenData coursewareGenData) throws IOException {
        // 解析
        InputStream pptIs = createPpt(coursewareGenData.getPresentationData());
        InputStream wordIs = createWord(coursewareGenData.getWordData());
        String pptName = coursewareGenData.getPresentationData().getPptName();
        String wordName = coursewareGenData.getWordData().getWordName();

        // TODO 文件名生成
        if (pptName == null)
            pptName = "ppt" + System.currentTimeMillis() + ".pptx";
        if (wordName == null)
            wordName = "word" + System.currentTimeMillis() + ".docx";

        // 压缩
        String zipPath = FileZipper.compressInputStreamToZip(Map.of(pptName, pptIs, wordName, wordIs), exportFilePath, pptName + ".zip");

        // 存表 TODO

        return zipPath;
    }

    /**
     * 向前端 websocket 接口发送课件生成状态
     */
    @Override
    public void notifyCoursewareGeneratingStatus(GenerationStatus message) {
        CoursewareGenerationEndpoint.notifyGeneratingStatus(message);
    }

    @Override
    public String getPptHtml(Integer coursewareId) {
        String html = getPptSlidePage(coursewareId).innerHTML("body");
        if (html == null || html.isBlank())
            return null;

        return html;
    }

    @Override
    public String getWordHtml(Integer coursewareId) {
        String html = getDocumentPage(coursewareId).innerHTML("body");
        if (html == null || html.isBlank())
            return null;

        return html;
    }

    @Override
    public Page getPptSlidePage(Integer coursewareId) {
        return pptSlidePageCache.getOrCreate(coursewareId, playwrightManager::createPage);
    }

    @Override
    public Page getDocumentPage(Integer coursewareId) {
        return documentPageCache.getOrCreate(coursewareId, playwrightManager::createPage);
    }

    /**
     * 生成 PPT
     *
     * @return 文件名，包含扩展名。若生成错误，返回null
     */
    private InputStream createPpt(PresentationData presentationData) {
        if (presentationData == null) {
            log.error("生成PPT失败，presentationData为空");
            return null;
        }

        try {
            // 加载 PPT 初始文件 courseware-service/src/main/resources/office-files/ppt.pptx
            ClassPathResource resource = new ClassPathResource("office-files/ppt.pptx");
            InputStream pptInputStream = resource.getInputStream();

            XMLSlideShow ppt = new XMLSlideShow(pptInputStream);
            List<XSLFSlideMaster> slideMasters = ppt.getSlideMasters();
            Dimension pageSize = ppt.getPageSize();

            for (SlideData slideData : presentationData.getSlides()) {

                XSLFSlide slide = ppt.createSlide(slideMasters.get(0).getSlideLayouts()[0]);

                // 设置幻灯片备注
                setSlideNote(ppt, slide, slideData.getSlideNote());

                // 解析元素
                for (SlideElement slideElement : slideData.getSlideElements()) {
                    switch (slideElement.getElemType()) {
                        case TEXT ->
                                addTextParagraph(slideElement.getRect(), slideElement.asTextParagraph(), slide, pageSize);
                        case IMAGE ->
                                addImageElement(slideElement.getRect(), slideElement.asImageUrl(), ppt, slide, pageSize);
                        case TABLE ->
                                addTableElement(slideElement.getRect(), slideElement.asTableData(), slide, pageSize);
                        case VIDEO ->
                                addVideoElement(slideElement.getRect(), slideElement.asVideoData(), ppt, slide, pageSize);
                    }
                }

            }

            String storagePath = "D:\\Desktop\\test1.pptx";

            // 保存到磁盘
            try (FileOutputStream fileOutputStream = new FileOutputStream(storagePath)) {
                ppt.write(fileOutputStream);
                ppt.close();
            }
            pptInputStream.close();

            return new FileInputStream(storagePath); // TODO

        } catch (Exception e) {
            log.error("保存PPT文件失败", e);
            return null;
        }
    }


    /**
     * 生成Word
     *
     * @param wordData
     * @return 文件名，包含扩展名。若生成错误，返回null
     */
    public InputStream createWord(WordData wordData) {

        if (wordData == null) {
            log.error("生成Word失败，wordData为空");
            return null;
        }

        XWPFDocument document = new XWPFDocument();

        XWPFNumbering docNumbering = document.createNumbering();

        List<WordElement> wordElements = wordData.getWordElements();

        for (WordElement wordElement : wordElements) {
            // 创建段落
            switch (wordElement.getType()) {
                case TEXT -> {
                    WordParagraph wordParagraph = wordElement.asWordParagraph();
                    createWordParagraph(wordParagraph, document, docNumbering);
                }
                // 创建图片
                case IMAGE -> {
                    WordImage wordImage = wordElement.asWordImage();
                    addWordImage(document, wordImage);
                }
                // 创建表格
                case TABLE -> {
                    TableData tableData = wordElement.asTableData();
                    WordTableUtils.createTableElement(tableData, document);
                }
            }
        }

        try {
            String storagePath = "D:\\Desktop\\test1.docx";
            FileOutputStream outputStream = new FileOutputStream(storagePath);
            document.write(outputStream);
            outputStream.close();
            document.close();

            return new FileInputStream(storagePath);
        } catch (Exception e) {
            log.error("保存Word文件失败", e);
            return null;
        }

    }

    @Override
    public List<Document> searchUserDocuments(Integer userId, String query) {
        return List.of();
    }

    @Override
    public List<Document> searchGlobalDocuments(String query) {
        return List.of();
    }

    @Override
    public String generateImageUrl(String prompt, int width, int height) {
        return "";
    }

}
