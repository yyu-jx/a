# 模块说明: 课件生成服务 courseware-service

## 依赖

此模块依赖AI模块。

此模块引入了Playwright for Java无头浏览器，用于渲染并解析布局。在首次运行时，它会在你的电脑上安装Playwright。

## 功能说明

此模块主要提供两个功能：PPT&DOCX的生成与解析。

`com.superyoudi.coursewareservice.ai`模块存放工作流编排的节点与工具。

`com.superyoudi.coursewareservice.config.GraphConfig`编排工作流。

> 交互流程：
> 
> 用户输入传入接口，调用AI。监督者节点负责用处理用户交互。当监督者搜集了所需的信息，它会将数据传入两个并行节点：HtmlPptGenNode与HtmlWordGenNode。
> 
> 并行节点可根据用户需求选择性地执行其中一个节点。
> 
> HtmlPptGenNode与HtmlWordGenNode分别负责生成PPT与DOCX。其中，HtmlPptGenNode被包含在一个子图里。
> 数据传入子图时，将由PptOutlineGenNode生成PPT大纲，随后，子图执行中断，中断消息会被发送到前端，用户审查生成的PPT大纲并提出意见。
> 若允许，则继续生成；若提出修改，则将修改意见返回给大纲生成节点，并重新执行中断；若拒绝，则取消PPT生成。
> 
> 生成过程中，通过布局检查的HTML数据将通过Websocket实时发送给前端执行预览。
> 
> 生成完成后，完整的数据会发送给前端。（TODO 存数据）

`com.superyoudi.coursewareservice.utils`主要存放了对于DOM操作、布局检查、媒体处理的工具类。

`com.superyoudi.coursewareservice.websocket`存放Websocket的端点及DTO，正在开发中。
