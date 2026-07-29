package com.superyoudi.coursewareservice.ai.tools;


import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.stream.Collectors;

@Slf4j
@Deprecated
public class RagTools {

    private final CoursewareGenerationService coursewareGenerationService;

    public RagTools(CoursewareGenerationService coursewareGenerationService) {
        this.coursewareGenerationService = coursewareGenerationService;
    }

    /// 获取用户上传的文件列表，并格式化
    ///
    /// 使用文件名，文件描述，文件路径
    ///
    /// 格式化的信息如下：
    /// ```text
    /// 所有文件信息：
    ///
    /// 文件1
    /// 描述：xxxxxxxx
    /// 路径：xxx/xxx/xxx.xxx
    /// ---
    /// 文件2
    /// 描述：xxxxxxxx
    /// 路径：xxx/xxx/xxx.xxx
    /// ```
//    @Tool(name = "get_user_files", description = "获取用户上传的所有文件信息，包含文件名，描述，文件路径")
//    @SuppressWarnings("unused")
//    public String getUserFiles(@ToolParam(description = "用户ID") Integer userId) {
//        List<FileInfo> userFiles = aiAgentService.getUserFileInfos(userId);
//
//        StringBuilder sb = new StringBuilder("所有文件信息：\n\n");
//        for (FileInfo fileInfo : userFiles) {
//            sb.append(fileInfo.getFileName()).append("\n")
//                    .append("描述：").append(fileInfo.getFileDescription()).append("\n")
//                    .append("文件路径：").append(fileInfo.getFilePath()).append("\n")
//                    .append("---").append("\n");
//        }
//
//        String info = sb.toString();
//
//        log.info("用户文件信息：{}", info);
//
//        return info;
//    }

    /// 查询用户上传的资料数据库，并转换为提示词注入
    ///
    /// 输出格式如下:
    /// ```text
    /// 用户的参考资料:
    ///
    /// 文本文本文本文本文本文本文本文本
    /// ---
    /// 文本文本文本文本文本文本文本文本
    /// ---
    /// 文本文本文本文本文本文本文本文本
    /// ```
    ///
    /// @param query 查询的文本
    @Tool(name = "get_user_references", description = "查询用户上传的资料数据库作为优先参考")
    @SuppressWarnings("unused")
    public String getUserReferences(@ToolParam(description = "两三个查询的关键词，需要进行总结") String query) {
        // TODO userid
        String queryResult = coursewareGenerationService.searchUserDocuments(1, query)
                .stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        log.info("查询query: {}; 用户查询结果：{}", query, queryResult);

        return "用户的参考资料:\n\n" + queryResult;
    }


    /// 查询知识库，并转换为提示词注入
    ///
    /// 输出格式如下:
    /// ```text
    /// 用户的参考资料:
    ///
    /// 文本文本文本文本文本文本文本文本
    /// ---
    /// 文本文本文本文本文本文本文本文本
    /// ---
    /// 文本文本文本文本文本文本文本文本
    /// ```
    ///
    /// @param query 查询的文本
    @SuppressWarnings("unused")
    @Tool(name = "get_knowledge_base_references", description = "查询知识库的资料作为参考信息")
    public String getKnowledgeBaseReferences(@ToolParam(description = "两三个查询的关键词") String query) {
        String queryResult = coursewareGenerationService.searchGlobalDocuments(query)
                .stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        return "知识库参考资料:\n\n" + queryResult;
    }
}
