package com.superyoudi.coursewareservice.ai.tools;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.superyoudi.coursewareservice.utils.HtmlDomUtils;
import com.superyoudi.coursewareservice.utils.HtmlLayoutUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class HtmlWordTools {
    private final Page documentPage;

    public HtmlWordTools(Page documentPage) {
        this.documentPage = documentPage;
    }

    @SuppressWarnings("unused")
    @Tool(name = "create_document", description = "创建文档的容器")
    public String createDocument(@ToolParam(description = """
            文档的容器。格式如下:
            <div class="page"
                style="width: 210mm;
                min-height: 297mm;
                padding: 25.4mm 31.7mm;
                background-color: white;
                box-sizing: border-box;"
            >
            </div>
            """) String html) {
        HtmlDomUtils.setPageHtml(documentPage, html);

        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "append_paragraph", description = "在文档末尾追加段落元素")
    public String appendParagraph(@ToolParam(description = "新元素的HTML文本，必须符合规范") String html) {
        Locator elem = getDocumentContainer();
        if (elem == null) {
            return "未找到元素";
        }

        HtmlDomUtils.appendChild(elem, html);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "insert_after", description = "在指定元素之后插入元素")
    public String insertAfterElement(@ToolParam(description = "目标元素名称") String elementName,
                                     @ToolParam(description = "要插入的HTML文本，必须符合规范") String html) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        HtmlDomUtils.insertAfter(element, html);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "insert_before", description = "在指定元素之前插入元素")
    public String insertBeforeElement(@ToolParam(description = "目标元素名称") String elementName,
                                      @ToolParam(description = "要插入的HTML文本，必须符合规范") String html) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        HtmlDomUtils.insertBefore(element, html);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "replace_element", description = "整体替换指定元素")
    public String replaceElement(@ToolParam(description = "目标元素名称") String elementName,
                                 @ToolParam(description = "新的HTML文本，必须符合规范") String html) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        HtmlDomUtils.replaceElement(element, html);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "replace_outer_html", description = "整体替换指定元素的外部HTML")
    public String replaceOuterHtml(@ToolParam(description = "目标元素名称") String elementName,
                                   @ToolParam(description = "新的HTML文本，必须符合规范") String html) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        HtmlDomUtils.replaceOuterHtml(element, html);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "remove_element", description = "移除指定元素")
    public String removeElement(@ToolParam(description = "目标元素名称") String elementName) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        HtmlDomUtils.removeElement(element);
        return "成功";
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_element_html", description = "获取指定元素的HTML文本")
    public String getElementHtml(@ToolParam(description = "目标元素名称") String elementName) {
        Locator element = HtmlLayoutUtils.getLocatorByAttributeName(documentPage, "data-element-name", elementName);
        if (element == null) {
            return "未找到元素";
        }
        return HtmlDomUtils.getElementHtml(element);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_document_structure", description = "获取文档内容与结构")
    public String getDocumentStructure() {
        return HtmlDomUtils.traverseElementInfo(documentPage, ".page");
    }

    private Locator getDocumentContainer() {
        return documentPage.locator(".page");
    }

    public String getHtml() {
        return documentPage.innerHTML("body");
    }
}
