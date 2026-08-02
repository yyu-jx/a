package com.superyoudi.aiservice.pojo.entity;

import lombok.Data;

/**
 * @author Sijayyx
 */
@Data
public class ChatCourseRel {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 会话id
     */
    private Long chatId;
    /**
     * 课程id
     */
    private Long courseId;
    /**
     * 冗余学生ID
     */
    private Long userId;
}