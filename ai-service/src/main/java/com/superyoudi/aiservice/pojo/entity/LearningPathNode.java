package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习路径节点实体
 * 对应数据库 learning_path_nodes 学习路径‑节点表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPathNode {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 关联学习路径id
     */
    private Long pathId;

    /**
     * 知识点id，用于查询学生知识点学习情况
     */
    private String knowledgeId;

    /**
     * 路径节点完成状态
     * untested 未进行 | proceeding 进行中 | completed 完成
     */
    private String status;

    /**
     * 节点关联作业id，作业正确率≥60%才可完成该节点
     */
    private Long relatedTaskId;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}