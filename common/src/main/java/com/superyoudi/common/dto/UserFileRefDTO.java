package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFileRefDTO {
    private Long fileRefRelId;
    private Long fileId;
    private String fileName;
    private String fileDesc;
    private String fileUrl;
    private String type;
}
