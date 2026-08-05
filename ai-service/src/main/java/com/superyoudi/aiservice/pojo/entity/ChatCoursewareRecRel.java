package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话 课件生成 关联表
 *
 * @author Sijayyx
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCoursewareRecRel {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long chatId;

    /**
     * 课件生成记录ID
     */
    private Long coursewareRecordId;

    /**
     * 教师ID
     */
    private Long userId;
}