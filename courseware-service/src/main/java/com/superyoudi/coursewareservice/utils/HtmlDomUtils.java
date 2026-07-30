package com.superyoudi.coursewareservice.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Arrays;

/**
 * 这个工具类提供了操作Locator DOM元素的便捷方法。
 */
public class HtmlDomUtils {

    /**
     * 设置页面的HTML文本
     *
     * @param page
     * @param html
     */
    public static void setPageHtml(Page page, String html) {
        page.evaluate("""
                (html) => {
                    document.body.innerHTML = html;
                }
                """, html);
    }

    /**
     * 在父元素的子元素列表的指定位置插入HTML
     *
     * @param parent
     * @param childHtml
     * @param index
     */
    public static void insertChildAt(Locator parent, String childHtml, int index) {
        parent.evaluate("""
                (parent, [index, htmlString]) => {
                    const children = parent.children;
                
                    // 边界处理
                    if (index < 0) index = 0;
                    if (index >= children.length) {
                        // 插入到末尾
                        parent.insertAdjacentHTML('beforeend', htmlString);
                    } else {
                        // 在指定子元素之前插入
                        children[index].insertAdjacentHTML('beforebegin', htmlString);
                    }
                }
                """, Arrays.asList(childHtml, index));
    }

    /**
     * 在子元素列表的末尾添加HTML
     *
     * @param parent
     * @param childHtml
     */
    public static void appendChild(Locator parent, String childHtml) {
        parent.evaluate("""
                (element, html) => {
                    element.insertAdjacentHTML('beforeend', html);
                }
                """, childHtml);
    }

    /**
     * 在元素的前方添加HTML
     */
    public static void insertAfter(Locator element, String html) {
        element.evaluate("""
                (element, html) => {
                    element.insertAdjacentHTML('afterend', html);
                }
                """, html);
    }

    /**
     * 在元素的后方添加HTML
     */
    public static void insertBefore(Locator element, String html) {
        element.evaluate("""
                (element, html) => {
                    element.insertAdjacentHTML('beforebegin', html);
                }
                """, html);
    }

    /**
     * 替换元素
     */
    public static void replaceElement(Locator element, String html) {
        element.evaluate("""
                (element, html) => {
                    element.outerHTML = html;
                }
                """, html);
    }

    /**
     * 替换元素并保留内部内容
     */
    public static void replaceOuterHtml(Locator element, String html) {
        element.evaluate("""
                (el, html) => {
                     const newEl = new DOMParser()
                       .parseFromString(html, 'text/html')
                       .body.firstElementChild;
                
                     // 搬移内部内容
                     newEl.innerHTML = el.innerHTML;
                     // 替换
                     el.replaceWith(newEl);
                 }
                """, html);
    }

    /**
     * 删除元素
     */
    public static void removeElement(Locator element) {
        element.evaluate("""
                (element) => {
                    element.remove();
                }
                """);
    }

    /**
     * 获取元素HTML
     */
    public static String getElementHtml(Locator element) {
        return (String) element.evaluate("""
                (element) => {
                    return element.outerHTML;
                }
                """);
    }

    /**
     * 递归遍历元素结构并输出信息
     */
    public static String traverseElementInfo(Page page, String rootSelector) {
        return (String) page.evaluate("""
                selector => {
                  let result = '';
                  let root = document.querySelector(selector);
                
                  function traverse(node, depth) {
                    if (node.nodeType === 1) {  // Element 节点
                      // 缩进
                      result += '\\t'.repeat(Math.max(0, depth));
                
                      // 获取自定义属性
                      let type = node.hasAttribute('data-element-type') ? node.getAttribute('data-element-type') : '无';
                      let name = node.hasAttribute('data-element-name') ? node.getAttribute('data-element-name') : '无';
                      let desc = node.hasAttribute('data-element-desc') ? node.getAttribute('data-element-desc') : '无';
                
                      // 输出格式：<标签名>, 类型：值, 名称：值
                      result += '<' + node.tagName.toLowerCase() + '>';
                      result += ', 类型：' + type;
                      result += ', 名称：' + name;
                      result += ', 描述：' + desc;
                      result += '\\n';
                
                      // 递归子节点
                      for (let child of node.children) {
                        traverse(child, depth + 1);
                      }
                    }
                  }
                
                  if (root) {
                    traverse(root, 0);
                  }
                
                  return result;
                }""", rootSelector);
    }

    /**
     * 根据名称获取HTML属性值
     */
    public static Object getHtmlAttributeValue(Page page, String html, String attributeName) {
        return page.evaluate(
                String.format(
                        """
                                html => {
                                  let parser = new DOMParser();
                                  let doc = parser.parseFromString(html, 'text/html');
                                  let element = doc.querySelector('[%s]');
                                  return element ? element.getAttribute('%s') : null;
                                }
                                """, attributeName, attributeName), html);
    }
}
