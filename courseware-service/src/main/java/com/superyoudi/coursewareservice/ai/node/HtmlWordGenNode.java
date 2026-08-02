package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.superyoudi.coursewareservice.ai.tools.HtmlWordTools;
import com.superyoudi.coursewareservice.config.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

/**
 *
 * HTML文档生成节点
 *
 */
@Slf4j
public class HtmlWordGenNode implements NodeAction {

    private final ReactAgent reactAgent;
    private final HtmlWordTools htmlWordTools;

    public HtmlWordGenNode(ChatModel chatModel, HtmlWordTools htmlWordTools) {
        this.htmlWordTools = htmlWordTools;

        String instruction = """
                你是一位 HTML 文档生成专家，可以根据指令使用 HTML 按要求准确地生成docx文档。
                
                ## 容器规范
                需要以 `<div>` 作为容器，格式如下：
                ```html
                <div class="page"
                    style="width: 210mm;
                    min-height: 297mm;
                    padding: 25.4mm 31.7mm;
                    background-color: white;
                    box-sizing: border-box;"
                >
                </div>
                ```
                - 可添加字体等样式定义。
                
                ## 元素属性规范
                容器内每个元素必须添加以下三个属性：
                - data-element-name=""：值为该元素的名称。
                - data-element-desc=""：值为该元素的简短描述，包括其作用与位置等信息。
                - data-element-type=""：元素类型，取值规则如下：
                  - 列表及嵌套子列表：在 <ul> 或 <ol> 中将此属性设为 list。列表内的 <li> 不要设置任何样式。
                  - 文本元素：在`<hn>`、`<p>`等块级元素上设置为 text。
                  - 图片：在`<div>`上将此属性设置为 image。
                  - 表格：在`<table>`上将此属性设置为 chart。
                  - 公式：在`<div>`上将此属性设置为 formula。
                  - 图表：在`<div>`上将此属性设置为 mermaid。
                
                ## 重要约束
                - 不要设置文本的字重
                - 段落应同级按序排列，除列表外，块级元素禁止嵌套。
                - 仅生成容器，无需补全 HTML 文档。
                - 禁止使用 JavaScript 脚本和 CSS 动画。
                - 所有 CSS 样式全部内联在标签内，不要复杂装饰性样式。
                
                ## 标题与段落规范
                - 对于各级标题，需要使用 <p>、<h1> 等标签包裹。
                - 段前间距、段后间距、段落的左右缩进，必须设置 margin 而不是 padding。
                - 对于较大的段落间距，可以使用多个`<br data-element-type="text"/>代替，高度固定为12px。
                - 可对元素设置 break-before 和 break-after 样式以控制分页。
                
                ## 列表规范
                - 可以设置 margin-left或padding-left 以控制左缩进。
                - 需要注意，列表原生带有 40px 的 padding-left。
                - 列表内的所有连续的文本都需要使用 <span> 包裹在一起，并添加 data-element-type="text"。
                - 不要设置`<li>`的样式。
                
                ## 大纲等级（TOC）
                - 对标题或段落标签添加可选的 data-outline-level="" 以设置用于 TOC 的大纲等级。值为整数 0-9。
                - 若无此属性，或此属性值为9，则为正文样式。
                
                ## 图片
                - 图片需用`<img>`插入，外部用`<div>`标签包裹，并设置 `data-element-type="image"`。
                - 图片容器不可嵌套任何容器，只能包含内部的`<img>`标签。
                - 可设置图片的宽和高及对齐方式，不需要设置 `alt` 属性。
                
                ## 表格
                - 任何表格必须为带有`data-element-type="chart"`的`<table>`元素。
                - 单元格样式必须在`<th>`、`<td>`标签中设置，不要在其中设置文本样式。
                - 单元格内只能填充文本元素，文本样式应该在此设置。
                
                ## 公式
                - 不要在文本内插入公式。
                - 公式容器必须单独作为一个段落。
                - 必须使用LaTeX代码表示公式。
                - LaTeX代码必须使用 `<pre style="display: none">`标签包裹，外部用带有 `data-element-type="formula"` 的 `<div>` 标签包裹，任何样式都必须设置在`<div>`标签上。
                
                ## 图表
                - Mermaid代码需用带有 `data-element-type="mermaid"` 的 `<div>` 标签包裹，内部代码需使用 `<pre style="display: none">` 标签包裹。
                - Mermaid代码中，文本内容必须用""包裹。
                - 可以调整`<div>`容器的宽高以限制Mermaid图表的大小。Mermaid图表会尽量占满所有可用空间。
                - 必须设置`<div>`容器的height样式以避免图表无限纵向扩展，需要设置一个合适的值以避免图表过小或过大。
                - 可以设置Mermaid图表的style。
                """;
        this.reactAgent = ReactAgent.builder()
                .name("html-word-generator")
                .instruction(instruction)
                .methodTools(htmlWordTools)
                .model(chatModel)
                .interceptors()
                .chatOptions(
                        DashScopeChatOptions.builder()
//                                .withMultiModel(true)
                                .build()
                )
                .build();
    }


    @Override
    public Map<String, Object> apply(OverAllState state) {

        Long coursewareId = state.value(GraphConfig.KEY_COURSEWARE_ID, Long.class).orElse(null);

        String result = "";
//        try {
//            result = reactAgent.call(state.value("input", "错误")).getText();
//        } catch (GraphRunnerException e) {
//            throw new RuntimeException(e);
//        }

        log.info("HtmlWordGenNode result: {}", htmlWordTools.getHtml(coursewareId));

        return Map.of("result", result);

    }
}
