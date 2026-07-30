package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课件生成记录表 courseware_records
 * AI课件生成任务记录，任务启动即创建该记录
 */
@Data
public class CoursewareRecord {
    /** 主键id */
    private Long id;
    /** 操作教师用户id */
    private Long userId;
    /** 关联AI对话会话id */
    private Long chatId;
    /** 导出PPT文件id，未导出为null，关联user_files.id */
    private Long pptFileId;
    /** 导出Word教案文件id，未导出为null，关联user_files.id */
    private Long wordFileId;
    /** AI生成PPT原始HTML代码 */
    private String pptHtml;
    /** AI生成教案原始HTML代码 */
    private String wordHtml;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
}