package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathNodeTaskRel {
    private Long id;
    private Long studentId;
    private Long pathNodeId;
    private Long taskId;
    private LocalDateTime createAt;
}
