package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资料文件表 user_files
 * 存储用户上传或者AI生成的文件记录
 */
@Data
public class UserFile {
    /** 主键id */
    private Long id;
    /** 文件所属用户id */
    private Long userId;
    /** 文件名称 */
    private String fileName;
    /** 文件描述 */
    private String fileDesc;
    /** 文件访问url地址 */
    private String fileUrl;
    /** 文件大小，单位Byte字节 */
    private Long size;
    /** 文件类型：pdf｜pptx｜docx｜media媒体｜other其他 */
    private String type;
    /** 文件来源：upload用户上传｜generated AI生成 */
    private String source;
    /** 更新时间 */
    private LocalDateTime updateAt;
    /** 创建时间 */
    private LocalDateTime createTime;
}