package com.superyoudi.aiservice.ai.tools;


import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

@Component
public interface RagTools {

    String queryKnowledgeGraph(String query);

    String queryKnowledgePoint(String knowledgeName);

    String addCitationToMessage(String title, String type, String json, ToolContext toolContext);

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


}
