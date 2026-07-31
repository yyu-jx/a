package com.superyoudi.coursewareservice.ai.tools;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import com.superyoudi.coursewareservice.utils.HtmlDomUtils;
import com.superyoudi.coursewareservice.utils.HtmlLayoutUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Component
public class HtmlPptTools {

    private final CoursewareGenerationService coursewareGenerationService;

    private final Page layoutPage;

    public HtmlPptTools(CoursewareGenerationService coursewareGenerationService, Page layoutPage) {
        this.coursewareGenerationService = coursewareGenerationService;
        this.layoutPage = layoutPage;
    }

    /**
     * 读取目录中所有的desc.md文件，并按照指定格式输出为字符串
     */
    private static String combineAllMdFiles(String directoryPath) throws IOException {
        StringBuilder result = new StringBuilder();

        Path startPath = Path.of(directoryPath);

        try (Stream<Path> paths = Files.walk(startPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals("desc.md"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p);

                            // 如果已经添加了内容，添加分隔符
                            if (!result.isEmpty()) result.append("\n---\n");

                            result.append(content);
                        } catch (IOException e) {
                            log.error("提取MD文件出现错误。", e);
                        }
                    });
        }

        return result.toString();
    }

    @SuppressWarnings("unused")
    @Tool(name = "add_slide", description = "按顺序添加一个幻灯片的基础容器")
    public String addSlideContainer(
            @ToolParam(description = "课件记录ID") Integer coursewareId,
            @ToolParam(description = """
                    页面的主容器。格式必须如下:
                    <div class="slide-container"
                         data-element-type="slide"
                         data-element-name=""
                         style=""
                         >
                    </div>
                    其中，data-element-name与data-element-type均为必须项
                    """) String html) {

        Locator bodyLocator = getUserSlidePage(coursewareId).locator("body");

        HtmlDomUtils.appendChild(bodyLocator, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "insert_slide", description = "在指定位置插入一个幻灯片的基础容器")
    public String insertSlide(@ToolParam(description = "课件记录ID") Integer coursewareId,
                              @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                              @ToolParam(description = """
                                      页面的主容器。格式必须如下:
                                      <div class="slide-container"
                                           data-element-type="slide"
                                           data-element-name=""
                                           style=""
                                           >
                                      </div>
                                      """) String html,
                              @ToolParam(description = "插入的位置索引，从0开始") Integer index) {

        Locator bodyLocator = getUserSlidePage(coursewareId).locator("body");
        HtmlDomUtils.insertChildAt(bodyLocator, html, index);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "remove_slide", description = "删除指定页面")
    public String removeSlideContainer(@ToolParam(description = "课件记录ID") Integer coursewareId,
                                       @ToolParam(description = "目标页面索引，从0开始") Integer slideId) {
        Locator slideLocator = HtmlLayoutUtils.getSlideLocator(getUserSlidePage(coursewareId), slideId);
        HtmlDomUtils.removeElement(slideLocator);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "reorder_slide", description = "重新排序多张幻灯片")
    public String reorderSlide(@ToolParam(description = "课件记录ID") Integer coursewareId,
                               @ToolParam(description = "要移动的幻灯片的起始索引，从0开始") Integer fromIndex,
                               @ToolParam(description = "移动的数量，最小为1") Integer length,
                               @ToolParam(description = "目标位置索引，从0开始") Integer targetIndex
    ) {
        Locator bodyLocator = getUserSlidePage(coursewareId).locator("body");

        bodyLocator.evaluate("""
                (parent, [fromIndex, length, toIndex]) => {
                    const children = parent.children;
                
                    // 边界检查
                    if (fromIndex + length > children.length ||
                        fromIndex < 0 || length <= 0 || toIndex < 0) {
                        return;
                    }
                
                    // 获取要移动的元素
                    const nodes = Array.from(children).slice(fromIndex, fromIndex + length);
                
                    // 创建文档片段批量操作
                    const fragment = document.createDocumentFragment();
                    nodes.forEach(node => fragment.appendChild(node));
                
                    // 计算插入位置
                    let insertIndex = toIndex > fromIndex ? toIndex - length : toIndex;
                    insertIndex = Math.min(insertIndex, children.length);
                
                    // 执行插入
                    parent.insertBefore(fragment, children[insertIndex] || null);
                }
                """, Arrays.asList(fromIndex, length, targetIndex));

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "append_child_element", description = "在指定父级元素下追加一个子元素")
    public String appendChildElement(@ToolParam(description = "课件记录ID") Integer coursewareId,
                                     @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                     @ToolParam(description = "追加的HTML元素。内容必须严格遵守指令。") String html,
                                     @ToolParam(description = "父级元素名称") String parentName) {

        log.info("appendElement:\n {}, parentName:\n {}", html, parentName);

        Locator parentLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", parentName);

        if (parentLocator == null)
            return "父级元素不存在";

        HtmlDomUtils.appendChild(parentLocator, html);

        String elementName = (String) HtmlDomUtils.getHtmlAttributeValue(getUserSlidePage(coursewareId), html, "data-element-name");

        StringBuilder sb = new StringBuilder();

        Locator slideLocator = HtmlLayoutUtils.getSlideLocator(getUserSlidePage(coursewareId), slideId);
        HtmlLayoutUtils.checkElementLayout(layoutPage, slideLocator, parentName, elementName, sb);

        log.info("innerHTML:\n {}", getUserSlidePage(coursewareId).innerHTML("body"));
        log.info("checkElementLayout:\n {}", sb);

        if (sb.isEmpty())
            return "成功";

        return "元素布局出现错误，请移除并重新插入对应元素:\n" + sb;
    }

    @SuppressWarnings("unused")
    @Tool(name = "replace_element", description = "替换指定元素")
    public String replaceElement(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                 @ToolParam(description = "替换的HTML元素。内容必须严格遵守指令。") String html,
                                 @ToolParam(description = "被替换的目标元素名称") String targetName) {

        log.info("replaceElement:\n {},\n targetName:\n {}", html, targetName);

        Locator targetLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", targetName);
        if (targetLocator == null)
            return "目标元素不存在";

        HtmlDomUtils.replaceElement(targetLocator, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "replace_outer_html", description = "只替换指定元素的外层HTML文本，保留内部元素")
    public String replaceOuterHtml(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                   @ToolParam(description = "新的HTML标签，不需要含有子元素。内容必须严格遵守指令。") String html,
                                   @ToolParam(description = "被替换的目标元素名称") String targetName) {

        Locator targetLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", targetName);
        if (targetLocator == null)
            return "目标元素不存在";

        HtmlDomUtils.replaceOuterHtml(targetLocator, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "insert_before_element", description = "在指定元素之前插入一个元素")
    public String insertBeforeElement(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                      @ToolParam(description = "插入的HTML元素。内容必须严格遵守指令。") String html,
                                      @ToolParam(description = "目标元素名称") String targetName) {

        log.info("insertBeforeElement: {}, targetName: {}", html, targetName);

        Locator targetLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", targetName);
        if (targetLocator == null)
            return "目标元素不存在";

        HtmlDomUtils.insertBefore(targetLocator, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "insert_after_element", description = "在指定元素之后插入一个元素")
    public String insertAfterElement(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                     @ToolParam(description = "插入的HTML元素。内容必须严格遵守指令。") String html,
                                     @ToolParam(description = "目标元素名称") String targetName) {

        log.info("insertAfterElement: {}, targetName: {}", html, targetName);

        Locator targetLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", targetName);

        if (targetLocator == null)
            return "目标元素不存在";

        HtmlDomUtils.insertAfter(targetLocator, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_element_structure", description = "获取指定元素的结构")
    public String getElementStructure(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面索引，从0开始") Integer slideId,
                                      @ToolParam(description = "元素名称") String elementName) {

        String string = HtmlDomUtils.traverseElementInfo(getUserSlidePage(coursewareId), String.format("[data-element-name=\"%s\"]", elementName));
        log.info("元素结构：{}", string);
        return string;
    }

    @SuppressWarnings("unused")
    @Tool(name = "remove_element", description = "删除指定元素")
    public String removeElement(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面ID") Integer slideId,
                                @ToolParam(description = "元素名称") String elementName) {

        Locator elementLocator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", elementName);

        if (elementLocator == null)
            return "元素不存在";

        HtmlDomUtils.removeElement(elementLocator);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_slide_structure", description = "获取整个页面的结构")
    public String getSlideStructure(@ToolParam(description = "课件记录ID") Integer coursewareId,
                                    @ToolParam(description = "目标页面索引，从0开始") Integer slideId
    ) {
        String string = HtmlDomUtils.traverseElementInfo(getUserSlidePage(coursewareId), String.format("body > :nth-child(%d)", slideId + 1));

        log.info("页面{}结构：{}", slideId, string);
        return string;
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_slide_list", description = "获取所有页面的索引及名称")
    public String getSlideList(@ToolParam(description = "课件记录ID") Integer coursewareId) {
        Locator bodyLocator = getUserSlidePage(coursewareId).locator("body");
        String slideNames = (String) bodyLocator.evaluate("""
                (element) => {
                    return Array.from(element.children)
                        .map((child, index) => {
                            const dataName = child.dataset.elementName;
                            return `索引: ${index}, 名称: ${dataName ? dataName : '无'}`;
                        })
                        .join('\\n');
                }
                """);

        log.info("页面列表：\n{}", slideNames);

        return "页面列表:\n" + slideNames;
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_element_html", description = "获取指定元素的HTML文本")
    public String getElementHtml(@ToolParam(description = "课件记录ID") Integer coursewareId, @ToolParam(description = "目标页面ID") Integer slideId,
                                 @ToolParam(description = "元素名称") String elementName) {

        Locator locator = HtmlLayoutUtils.getLocatorByAttributeName(getUserSlidePage(coursewareId), slideId, "data-element-name", elementName);

        if (locator == null)
            return "元素不存在";

        return HtmlDomUtils.getElementHtml(locator);
    }

    @Tool(name = "generate_image", description = """
            调用图片生成模型生成一张图片，返回图片的URL。
            尽量以16:9, 4:3或1:1生成图片。
            图片的最大宽高将被限制在1440px，最小宽高为512px。
            """)
    @SuppressWarnings("unused")
    public String generateImage(
            @ToolParam(description = "图片的描述提示词") String prompt,
            @ToolParam(description = "图片宽度，单位为像素") int width,
            @ToolParam(description = "图片高度，单位为像素") int height) {

        return coursewareGenerationService.generateImageUrl(prompt, width, height);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_user_files", description = "获取用户上传的所有文件信息，包含文件名，描述")
    public String getUserFiles(@ToolParam(description = "课件记录ID") Integer coursewareId) {
        return """
                URL: "../public/moon.png"
                描述: 一张月球的图片，宽高16:10
                """;
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_templates", description = "获取可用的模板列表")
    public String getTemplates() {
        try {
            // TODO 模板获取
            return combineAllMdFiles("E:\\DevStudy\\multi-modal-teach-ai-backend\\src\\main\\resources\\ppt-templates\\templates");
        } catch (IOException e) {
            return "读取失败!";
        }
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_html_template", description = "获取指定模板的HTML")
    public String getHtmlTemplate(@ToolParam(description = "模板名称") String templateName) {
        try {
            // TODO
            Path htmlFilePath = Path.of("E:\\DevStudy\\multi-modal-teach-ai-backend\\src\\main\\resources\\ppt-templates\\templates\\"
                    + templateName + "\\" + templateName + ".html");

            // 检查文件是否存在
            if (!Files.exists(htmlFilePath)) {
                String err = "文件不存在: " + htmlFilePath;
                log.error(err);
                return err;
            }

            // 读取并返回HTML内容
            return Files.readString(htmlFilePath, StandardCharsets.UTF_8);

        } catch (IOException e) {
            String err = "读取HTML文件失败: " + e.getMessage();
            log.error(err);
            return err;
        }
    }

    public String getHtml(@ToolParam(description = "课件记录ID") Integer coursewareId) {
        if (coursewareId == null) {
            return null;
        }

        return getUserSlidePage(coursewareId).innerHTML("body");
    }

    private Page getUserSlidePage(Integer coursewareId) {
        return coursewareGenerationService.getPptSlidePage(coursewareId);
    }
}
