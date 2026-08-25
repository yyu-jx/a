package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentVO {
    private Integer id;
    private String username;
    private String realname;
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}