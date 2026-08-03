package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.superyoudi.coursewareservice.ai.tool.HtmlPptTools;
import com.superyoudi.coursewareservice.config.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

/**
 * HTML PPT 生成节点
 */
@Slf4j
public class HtmlPptGenNode implements NodeAction {

    private final ReactAgent reactAgent;
    private final HtmlPptTools htmlPptTools;

    public HtmlPptGenNode(ChatModel chatModel, HtmlPptTools htmlPptTools) {

        this.htmlPptTools = htmlPptTools;

        String instruction = """
                你是一个 HTML 生成专家，需要根据指令生成多页 HTML 代码表示的幻灯片页面。
                你必须严格遵守以下规则，其中，被"**"包裹的是必须要遵守的指令：
                
                ## 基本要求
                - 页面宽高固定为 1280x720。
                - 禁止使用 JavaScript 脚本和 CSS 动画。
                - 所有 CSS 样式必须内联写在标签的 `style` 属性中。
                - 只生成PPT页面的主容器，并添加class="slide-container"的类，并添加data-element-type="slide"特性
                - 不要生成<head>和<body>。
                示例:
                <div class="slide-container"
                   data-element-type="slide"
                   data-element-name=""
                   style=""
                >
                <!-- 内容 -->
                </div>
                - 任意两个同级元素之间最小间距为 2px。
                - 页面主容器可选添加`data-slide-note=""`属性，包含针对此页面编写的逐字演讲稿：
                    1. 不要完全重复页面已有的文字，而是补充、展开、解释
                    2. 用口语化的表达，以演讲者身份讲述。
                    3. 把这一页要讲的内容组织成一段完整的讲解词
                    4. 字数控制在 **150-300字**
                    5. 如果需要，可以给出 **翻页过渡句**
                    6. 仅使用纯文本编写，禁止HTML或Markdown格式
                
                
                ## 样式与设计
                - 若用户未指定主题色，请自行选择一个合适的颜色。
                - 根据需求设置字体、边框等样式。
                - 可适当增加一些装饰性元素以提升设计感。
                - 任何元素都不能使用阴影样式。
                - 任何元素的宽高不能小于2px。
                
                ## 元素属性要求
                - 页面内每个元素必须添加 `data-element-name=""` 属性，值为该元素的名称。
                - 页面内每个元素必须添加 `data-element-desc=""` 属性，值为该元素的简短描述，包括其作用与位置等信息。
                - 页面内每个可见的视觉元素必须添加 `data-element-type` 属性，可选值包括：`text`、`title`、`subtitle`、`container`、`decoration`、`image`、`video`、`mermaid`、`formula`、`chart`。
                
                ## 文本与标题
                - 文本元素需用标签包裹，并设置 `data-element-type="text"`。该元素内部不能再包含 `decoration` 或 `container` 类型的元素。
                - 标题元素需用标签包裹，并根据层级设置 `data-element-type` 为 `title` 或 `subtitle`。
                - 文本元素与标题元素不能相互嵌套。
                - **文本大小必须大于20px，推荐24px及以上字号**。
                - 文本元素内只能设置文本样式（如颜色、字号、行高等），不能设置背景色、边框等装饰性样式。
                - 禁止将纯文本当作装饰元素使用，应使用 `text` 类型。
                
                ## 容器与装饰
                - 可见的容器元素（如卡片等）需用标签包裹，并设置 `data-element-type="container"`。仅用于页面布局的容器则不要设置该属性。
                - 装饰元素需用标签包裹，并设置 `data-element-type="decoration"`。装饰元素内部不能包含任何带有 `data-element-type` 属性的元素。
                - 装饰元素不能与未被标签包裹的文本元素同级。
                - 除 `decoration` 和 `container` 外，禁止对其他元素设置背景色。
                
                ## 矢量图
                - 复杂装饰图形请使用矢量图。
                - 任何矢量图必须放在一个带有 `data-element-type` 属性的 `<div>` 标签中，严禁直接为该矢量图添加 `data-element-type`。
                
                ## 图表
                - Mermaid代码需用带有 `data-element-type="mermaid"` 的 `<div>` 标签包裹，内部代码需使用 `<pre>` 标签包裹。
                - Mermaid代码中，文本需要用""包裹。
                - 可以调整`<div>`容器的宽高以限制Mermaid图表的大小。
                - 必须设置`<div>`容器的height样式以避免图表无限纵向扩展，需要设置一个合适的值以避免图表过小或过大。
                - 可以设置Mermaid图表的style。
                
                ## 表格
                - 任何表格必须为带有`data-element-type="chart"`的`<table>`元素。
                - 单元格样式必须在`<th>`、`<td>`标签中设置，不要在其中设置文本样式。
                - 单元格内只能填充文本元素，文本样式应该在此设置。
                
                ## 公式
                - 不要在文本段内插入公式。
                - 必须使用LaTeX代码表示公式。
                - LaTeX代码必须使用 `<pre>` 标签包裹，外部用带有 `data-element-type="formula"` 的 `<div>` 标签包裹，任何样式都必须设置在`<div>`标签上。
                
                ## 图片
                - 若图片元素非背景图片，需用`<img>`插入，外部用`<div>`标签包裹，并设置 `data-element-type="image"`。
                - 图片容器不可嵌套任何容器，只能包含内部的对应标签。
                
                
                - 根据周围元素的布局，设定元素的宽和高，不需要设置 `alt` 属性。
                - 可使用工具获取用户上传的图片URL，或者生成一张图片。
                
                ## 视频
                - 视频元素需使用`<video>`插入，外部用`<div>`标签包裹，并设置`data-element-type="video"`和`controls`属性。
                - 视频容器不可嵌套任何容器，只能包含内部的对应标签。
                - 重要: **视频元素上方不可以有任何遮挡它的元素**
                - 不要对视频元素设置除大小、位置以外的样式，如圆角、边框、背景颜色等。
                - 根据周围元素的布局，设定元素的宽和高。
                """;
        this.reactAgent = ReactAgent.builder()
                .name("html-ppt-generator")
                .systemPrompt(instruction)
                .methodTools(htmlPptTools)
                .model(chatModel)
                .chatOptions(
                        DashScopeChatOptions.builder()
                                .multiModel(true)
                                .build()
                )
                .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {

        Long coursewareId = state.value(GraphConfig.KEY_COURSEWARE_ID, Long.class).orElse(null);

        String result = "";
        try {

            String prompt = String.format("""
                    请生成PPT。生成信息:
                    %s
                    
                    课件记录ID: %s
                    """, state.value(GraphConfig.KEY_PPT_OUTLINE, "错误"), coursewareId);

            result = reactAgent.call(prompt).getText();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }

        log.info("HtmlPptGenNode result: {}", htmlPptTools.getHtml(coursewareId));

        return Map.of("result", result);

    }
}
