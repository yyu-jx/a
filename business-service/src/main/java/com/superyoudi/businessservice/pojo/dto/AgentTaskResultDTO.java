package com.superyoudi.businessservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class AgentTaskResultDTO<T> {

    /// 附带的数据为Task
    public static final String TYPE_TASK = "task";

    /// 附带的数据为CoursewareGenerationRequest.DTO
    public static final String TYPE_COURSEWARE_GEN_REQUEST = "courseware_gen_request";

    private String type;
    private T data;
}
