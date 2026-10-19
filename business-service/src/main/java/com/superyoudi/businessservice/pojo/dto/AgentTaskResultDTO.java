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

    private String type;
    private T data;
}
