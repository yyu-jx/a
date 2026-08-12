package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.mapper.UserFileMapper;
import com.superyoudi.businessservice.pojo.entity.UserFile;
import com.superyoudi.common.constants.UserFileConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final UserFileMapper userFileMapper;

    @Value("${app.storage.uploads}")
    private String uploadPath;

    @Value("${app.storage.exports}")
    private String exportPath;

    /**
     * 上传文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserFile(@NonNull MultipartFile file, @NonNull Long userId, String fileDesc, @NonNull String source) {
        // 存硬盘，存表
        Assert.notNull(file, "文件不能为null");
        Assert.notNull(userId, "userId不能为null");
        Assert.notNull(source, "文件来源不能为null");

        String originalName = file.getOriginalFilename(); // 原始文件名
        String extension = ""; // 扩展名
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // 生成唯一名称
        String uniqueName = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;

        String path;
        if (source.equals(UserFileConstants.SOURCE_UPLOAD))
            path = uploadPath;
        else if (source.equals(UserFileConstants.SOURCE_GENERATED))
            path = exportPath;
        else
            throw new IllegalArgumentException("文件来源不合法");

        Path savePath = Paths.get(path);
        // 存硬盘
        Path filePath = savePath.resolve(uniqueName);
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            log.error("文件存放失败", e);
            throw new RuntimeException("文件存放失败", e);
        }

        // 存表
        String fileUrl = path + "/" + uniqueName;
        Long size = file.getSize();
        String type = extractFileExtensionName(originalName);

        UserFile userFile = UserFile.builder()
                .userId(userId)
                .fileName(originalName)
                .fileDesc(fileDesc)
                .fileUrl(fileUrl)
                .size(size)
                .type(type)
                .source(source)
                .build();

        Long rows = userFileMapper.insert(userFile);
        if (rows == null || rows == 0)
            throw new RuntimeException("文件保存失败");
    }

    /**
     * 获取用户文件列表
     */
    public List<UserFile> getUserFileList(@NonNull Long userId) {
        Assert.notNull(userId, "userId不能为null");
        return userFileMapper.findByUserId(userId);
    }

    /**
     * 删除文件表及物理文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserFile(@NonNull Long fileId) {
        Assert.notNull(fileId, "fileId不能为null");

        UserFile userFile = userFileMapper.findById(fileId);

        if (userFile == null) {
            log.error("未找到对应的文件: {}", fileId);
            return;
        }

        // 删除物理文件
        Path filePath = Paths.get(userFile.getFileUrl());
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else
                log.warn("文件 {} 不存在", filePath);
        } catch (IOException e) {
            log.error("删除文件失败", e);
        }

        // 无论如何，都删除表记录
        userFileMapper.deleteById(fileId);
    }

    private String extractFileExtensionName(@Nullable String fileName) {
        if (fileName == null) return UserFileConstants.TYPE_OTHER;

        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (ext) {
            case "pdf" -> UserFileConstants.TYPE_PDF;
            case "docx" -> UserFileConstants.TYPE_DOCX;
            case "pptx" -> UserFileConstants.TYPE_PPTX;
            case "mp4",
                 "mp3",
                 "mkv",
                 "wav" -> UserFileConstants.TYPE_MEDIA;
            default -> UserFileConstants.TYPE_OTHER;
        };

    }
}
