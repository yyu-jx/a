package com.superyoudi.coursewareservice.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件打包工具
 * 核心功能：将文件列表打包到【源文件所在目录】
 */
@Slf4j
@Component
public class FileZipper {
    /// 缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    /**
     * 压缩文件流列表为 ZIP 包
     *
     * @param fileMap 文件映射列表，包含文件名和对应的文件流
     * @param zipName 压缩包名称。若为null，则自动生成文件名。
     * @return zip文件名，附带扩展名
     * @throws IOException 压缩过程中发生的 IO 异常
     */
    public static String compressInputStreamToZip(@NotNull Map<String, InputStream> fileMap,
                                                  @Nullable String zipName,
                                                  @NotNull String savePath) throws IOException {
        if (fileMap == null || fileMap.isEmpty()) {
            throw new IllegalArgumentException("文件映射不能为空");
        }

        // 生成压缩包名称
        String finalZipName = generateZipName(zipName);

        if (savePath == null || savePath.trim().isEmpty()) {
            throw new IllegalArgumentException("保存路径不能为空");
        }

        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            if (!saveDir.mkdirs()) {
                throw new IOException("无法创建保存目录：" + savePath);
            }
        }

        String fullPath = savePath + File.separator + finalZipName;

        // 创建 ZIP 文件输出流
        try (FileOutputStream fos = new FileOutputStream(fullPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // 遍历文件映射，写入 ZIP
            for (Map.Entry<String, InputStream> entry : fileMap.entrySet()) {
                String fileName = entry.getKey();
                InputStream is = entry.getValue();

                if (fileName == null || fileName.trim().isEmpty()) {
                    throw new IllegalArgumentException("文件名不能为空");
                }

                if (is == null) {
                    log.error("文件 '{}' 的流为空，跳过该文件", fileName);
                    continue;
                }

                // 创建 ZIP 条目
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                // 拷贝流内容到 ZIP
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                is.close(); // 关闭当前文件流
            }
        }

        return finalZipName;
    }

    /**
     * 生成压缩包文件名
     */
    private static String generateZipName(String zipName) {

        if (zipName == null || zipName.trim().isEmpty()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = String.valueOf((int) (Math.random() * 10000));
            return "zip_" + timestamp + "_" + random + ".zip";
        }

        if (zipName.toLowerCase().endsWith(".zip")) {
            return zipName;
        }
        return zipName + ".zip";
    }

    /**
     * 对文件进行打包
     *
     * @param fileNames 文件名称列表, 带后缀
     * @param zipName   压缩包名称（无需带.zip）
     * @return 是否打包成功
     */
    @Deprecated
    public boolean zipFilesToSourceDir(Collection<String> fileNames, String zipName) {
        // 自动补充 .zip 后缀
        if (!zipName.endsWith(".zip")) zipName += ".zip";

        // 获取目标目录
        Path targetDir = Path.of(null);

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