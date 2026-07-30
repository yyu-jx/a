package com.superyoudi.coursewareservice.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HtmlLayoutUtils {

    public static Locator getLocatorByAttributeName(Page page, int slideId, String attributeName, String attributeValue) {
        Locator targetSlide = getSlideLocator(page, slideId);

        if (targetSlide.getAttribute("data-element-name").equals(attributeValue))
            return targetSlide;

        List<Locator> locators = targetSlide.locator(String.format("[%s=\"%s\"]", attributeName, attributeValue)).all();
        if (locators.isEmpty()) {
            return null;
        }
        // 获取第一个
        return locators.get(0);
    }

    public static Locator getLocatorByAttributeName(Page page, String attributeName, String attributeValue) {
        List<Locator> locators = page.locator(String.format("[%s=\"%s\"]", attributeName, attributeValue)).all();
        if (locators.isEmpty()) {
            return null;
        }
        // 获取第一个
        return locators.get(0);
    }

    public static Locator getSlideLocator(Page page, int slideId) {
        Locator targetSlide = page.locator(String.format("body > :nth-child(%d)", slideId + 1));

        if (targetSlide == null)
            throw new RuntimeException("指定幻灯片不存在");

        return targetSlide;
    }

    public static void checkElementLayout(Page layoutPage, Locator slideLocator,
                                          String parentName, String locatorName, StringBuilder output) {
        layoutPage.evaluate("""
                (html) => {
                    document.body.innerHTML = html;
                }
                """, slideLocator.evaluate("el => el.outerHTML"));

        Locator parentLocator = getLocatorByAttributeName(layoutPage, "data-element-name", parentName);

        if (parentLocator == null)
            throw new RuntimeException("父级元素不存在");

        Locator locator = parentLocator.locator(String.format("[data-element-name=\"%s\"]", locatorName));

        traverseCheckElementLayout(parentLocator, parentName, locator, output, false);
    }

    /**
     * 遍历检查元素布局
     *
     * @param isStrict 是否启用严格模式（检查4px间距），false时仅检查重叠
     */
    private static void traverseCheckElementLayout(
            Locator parent,
            String parentName,
            Locator locator,
            StringBuilder output,
            boolean isStrict) {

        String elementName = locator.getAttribute("data-element-name");
        String elementType = locator.getAttribute("data-element-type");
        List<Locator> children = locator.locator("> *").all();
        BoundingBox boundingBox = locator.boundingBox();

        // 获取父容器和兄弟元素信息
        BoundingBox parentBox = null;
        List<Locator> siblings = null;
        if (parent != null) {
            parentBox = parent.boundingBox();
            siblings = parent.locator("> *").all();
        }

        // 检查非装饰元素
        if (elementType != null && !"decoration".equals(elementType)) {
            // 检查视口溢出
            String viewportError = checkViewportOverflow(elementName, boundingBox);
            if (!viewportError.isEmpty()) {
                output.append(viewportError);
            }

            // 检查父容器溢出
            String parentError = checkParentOverflow(elementName, parentName, boundingBox, parentBox);
            if (!parentError.isEmpty()) {
                output.append(parentError);
            }

            // 检查兄弟元素间距
            String spacingError = checkSiblingSpacing(elementName, locator, siblings, isStrict);
            if (!spacingError.isEmpty()) {
                output.append(spacingError);
            }
        }

        // 递归检查子元素
        for (Locator child : children) {
            traverseCheckElementLayout(locator, elementName, child, output, isStrict);
        }
    }

    /**
     * 检查元素是否超出视口边界
     *
     * @return 错误描述，无错误时返回空字符串
     */
    private static String checkViewportOverflow(String elementName, BoundingBox box) {
        if (box == null) {
            return "";
        }

        double pageWidth = 1280;
        double pageHeight = 720;
        StringBuilder errors = new StringBuilder();

        if (box.x < 0) {
            errors.append(String.format("   左边界超出：%.2fpx\n", Math.abs(box.x)));
        }
        if (box.y < 0) {
            errors.append(String.format("   上边界超出：%.2fpx\n", Math.abs(box.y)));
        }

        double rightEdge = box.x + box.width;
        if (rightEdge > pageWidth) {
            errors.append(String.format("   右边界超出：%.2fpx\n", rightEdge - pageWidth));
        }

        double bottomEdge = box.y + box.height;
        if (bottomEdge > pageHeight) {
            errors.append(String.format("   下边界超出：%.2fpx\n", bottomEdge - pageHeight));
        }

        return errors.isEmpty()
                ? ""
                : String.format("元素%s超出页面\n", elementName) + errors;
    }

    /**
     * 检查子元素是否超出父容器边界
     *
     * @return 错误描述，无错误时返回空字符串
     */
    public static String checkParentOverflow(
            String childName,
            String parentName,
            BoundingBox parentBox,
            BoundingBox childBox) {

        if (parentName == null || parentBox == null || childBox == null) {
            return "";
        }

        StringBuilder errors = new StringBuilder();

        double parentLeft = parentBox.x;
        double parentTop = parentBox.y;
        double parentRight = parentBox.x + parentBox.width;
        double parentBottom = parentBox.y + parentBox.height;

        double childLeft = childBox.x;
        double childTop = childBox.y;
        double childRight = childBox.x + childBox.width;
        double childBottom = childBox.y + childBox.height;

        if (childLeft < parentLeft) {
            errors.append(String.format("   左边界超出：%.2fpx\n", parentLeft - childLeft));
        }
        if (childTop < parentTop) {
            errors.append(String.format("   上边界超出：%.2fpx\n", parentTop - childTop));
        }
        if (childRight > parentRight) {
            errors.append(String.format("   右边界超出：%.2fpx\n", childRight - parentRight));
        }
        if (childBottom > parentBottom) {
            errors.append(String.format("   下边界超出：%.2fpx\n", childBottom - parentBottom));
        }

        return errors.isEmpty()
                ? ""
                : String.format("子元素%s(%.2f x %.2f) 超出 父容器%s(%.2f x %.2f):\n",
                childName, childBox.width, childBox.height,
                parentName, parentBox.width, parentBox.height) + errors;
    }

    /**
     * 检查元素与兄弟元素之间的间距
     *
     * @param elementName    当前元素名称
     * @param currentElement 当前元素的Locator
     * @param siblings       兄弟元素列表
     * @param isStrict       是否严格模式（检查4px最小间距）
     * @return 错误描述，无错误时返回空字符串
     */
    private static String checkSiblingSpacing(
            String elementName,
            Locator currentElement,
            List<Locator> siblings,
            boolean isStrict) {

        if (siblings == null || siblings.isEmpty() || currentElement == null) {
            return "";
        }

        BoundingBox currentBox = currentElement.boundingBox();
        if (currentBox == null) {
            return "";
        }

        StringBuilder output = new StringBuilder();

        for (Locator sibling : siblings) {
            try {
                String siblingName = sibling.getAttribute("data-element-name");
                String siblingType = sibling.getAttribute("data-element-type");

                // 跳过自身和装饰元素
                if (elementName.equals(siblingName) || "decoration".equals(siblingType)) {
                    continue;
                }

                BoundingBox siblingBox = sibling.boundingBox();
                if (siblingBox == null) {
                    continue;
                }

                String issue = checkTwoElementsLayout(
                        elementName, siblingName,
                        currentBox.x, currentBox.y, currentBox.width, currentBox.height,
                        siblingBox.x, siblingBox.y, siblingBox.width, siblingBox.height,
                        isStrict);

                if (!issue.isEmpty()) {
                    output.append(issue);
                }
            } catch (Exception ignored) {
                // 元素可能已失效，跳过
            }
        }

        return output.toString();
    }

    /**
     * 检查两个元素的布局关系
     * 核心检查逻辑：
     * - 总是检查重叠
     * - isStrict为true时额外检查4px最小间距
     *
     * @param isStrict 是否启用严格间距检查
     * @return 错误描述，无错误时返回空字符串
     */
    private static String checkTwoElementsLayout(
            String name1, String name2,
            double left1, double top1, double w1, double h1,
            double left2, double top2, double w2, double h2,
            boolean isStrict) {

        // 计算边界
        double right1 = left1 + w1;
        double bottom1 = top1 + h1;

        double right2 = left2 + w2;
        double bottom2 = top2 + h2;

        // 检查是否重叠
        boolean xOverlap = left1 < right2 && right1 > left2;
        boolean yOverlap = top1 < bottom2 && bottom1 > top2;

        if (xOverlap && yOverlap) {
            // 存在重叠，格式化重叠详情
            return formatOverlapDetails(name1, name2,
                    left1, right1, top1, bottom1,
                    left2, right2, top2, bottom2);
        }

        // 严格模式下检查4px最小间距
        if (isStrict) {
            return checkMinimumSpacing(name1, name2,
                    left1, right1, top1, bottom1,
                    left2, right2, top2, bottom2);
        }

        // 非严格模式且无重叠，返回空
        return "";
    }

    /**
     * 格式化重叠详情
     * 计算并描述各边的重叠情况
     */
    private static String formatOverlapDetails(
            String name1, String name2,
            double left1, double right1, double top1, double bottom1,
            double left2, double right2, double top2, double bottom2) {

        StringBuilder details = new StringBuilder();
        details.append(String.format("元素%s, %s重叠，请修改布局！各边重叠情况：\n", name1, name2));

        // 左侧重叠：元素1的右边覆盖元素2的左边
        if (right1 > left2 && left1 < left2) {
            double overlap = Math.min(right1, right2) - left2;
            details.append(String.format("  左侧重叠：%.2fpx\n", overlap));
        }

        // 右侧重叠：元素1的左边被元素2的右边覆盖
        if (left1 < right2 && right1 > right2) {
            double overlap = right2 - Math.max(left1, left2);
            details.append(String.format("  右侧重叠：%.2fpx\n", overlap));
        }

        // 上侧重叠：元素1的下边覆盖元素2的上边
        if (bottom1 > top2 && top1 < top2) {
            double overlap = Math.min(bottom1, bottom2) - top2;
            details.append(String.format("  上侧重叠：%.2fpx\n", overlap));
        }

        // 下侧重叠：元素1的上边被元素2的下边覆盖
        if (top1 < bottom2 && bottom1 > bottom2) {
            double overlap = bottom2 - Math.max(top1, top2);
            details.append(String.format("  下侧重叠：%.2fpx\n", overlap));
        }

        return details.append("\n").toString();
    }

    /**
     * 检查两个元素之间是否满足4px最小间距
     *
     * @return 错误描述，间距正常时返回空字符串
     */
    private static String checkMinimumSpacing(
            String name1, String name2,
            double left1, double right1, double top1, double bottom1,
            double left2, double right2, double top2, double bottom2) {

        final double MIN_SPACING = 4.0;

        // 检查水平间距
        if (right1 <= left2) {
            // 元素1在元素2左侧
            if (left2 - right1 < MIN_SPACING) {
                return String.format("元素%s与%s水平间距小于4px（当前%.2fpx）\n",
                        name1, name2, left2 - right1);
            }
        } else if (right2 <= left1) {
            // 元素2在元素1左侧
            if (left1 - right2 < MIN_SPACING) {
                return String.format("元素%s与%s水平间距小于4px（当前%.2fpx）\n",
                        name1, name2, left1 - right2);
            }
        }

        // 检查垂直间距
        if (bottom1 <= top2) {
            // 元素1在元素2上方
            if (top2 - bottom1 < MIN_SPACING) {
                return String.format("元素%s与%s垂直间距小于4px（当前%.2fpx）\n",
                        name1, name2, top2 - bottom1);
            }
        } else if (bottom2 <= top1) {
            // 元素2在元素1上方
            if (top1 - bottom2 < MIN_SPACING) {
                return String.format("元素%s与%s垂直间距小于4px（当前%.2fpx）\n",
                        name1, name2, top1 - bottom2);
            }
        }

        return "";
    }
}
