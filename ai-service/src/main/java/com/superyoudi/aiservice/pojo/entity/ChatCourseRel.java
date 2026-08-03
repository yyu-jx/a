package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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