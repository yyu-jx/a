package com.superyoudi.coursewareservice.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件打包工具
 * 核心功能：将文件列表打包到【源文件所在目录】
 */
@Slf4j
@Component
public class FileZipper {

    /// 文件类型: PPT
    public final static String FILE_PPT = "pptx";

    /// 文件类型: WORD
    public final static String FILE_WORD = "docx";

    /// 文件类型: ZIP
    public final static String FILE_ZIP = "zip";

    /// 缓冲区大小
    private static final int BUFFER_SIZE = 8192;

//    @Value("${file.storage}") TODO
    private String fileStoragePath;

    /**
     * 对文件进行打包
     *
     * @param fileNames 文件名称列表, 带后缀
     * @param zipName   压缩包名称（无需带.zip）
     * @return 是否打包成功
     */
    public boolean zipFilesToSourceDir(Collection<String> fileNames, String zipName) {
        // 自动补充 .zip 后缀
        if (!zipName.endsWith(".zip")) zipName += ".zip";

        // 获取目标目录
        Path targetDir = Path.of(fileStoragePath);

        // 压缩包存储路径
        Path zipPath = targetDir.resolve(zipName);

        log.info("开始打包文件到：{}", zipPath);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {

            for (String fileName : fileNames) {
                Path filePath = targetDir.resolve(fileName);
                if (!Files.exists(filePath)) continue;

                // 写入文件名
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);

                // 写入文件内容
                try (InputStream in = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                }

                zos.closeEntry();

            }

        } catch (IOException e) {
            log.error("压缩文件失败：{}", e.getMessage());
            return false;
        }
        log.info("打包完成：{}", zipPath);
        return true;
    }
}