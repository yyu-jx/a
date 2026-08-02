package com.superyoudi.coursewareservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.common.utils.MathUtils;
import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.xslf.usermodel.XSLFPictureData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 通用工具类
 * <p>
 * 其实是我也不知道这些方法要放在哪里
 *
 * @author Sijayyx
 */
public class UniversalUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取视频指定秒数的帧并保存为图片
     *
     * @param videoUrl 视频URL/路径
     * @param second   第几秒
     */
    public static BufferedImage extractVideoFrameAtSecond(String videoUrl, double second) throws IOException, InterruptedException {
        Demuxer demuxer;
        BufferedImage image = null;

        // 打开视频和初始化解码器
        demuxer = Demuxer.make();
        demuxer.open(videoUrl, null, false, true, null, null);

        // 查找视频流并初始化解码器
        int videoStreamIndex = -1;
        Decoder videoDecoder = null;

        for (int i = 0; i < demuxer.getNumStreams(); i++) {
            DemuxerStream stream = demuxer.getStream(i);
            Decoder decoder = stream.getDecoder();
            if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                videoStreamIndex = i;
                videoDecoder = decoder;
                break;
            }
        }

        if (videoDecoder == null)
            throw new RuntimeException("无视频流!");

        videoDecoder.open(null, null);

        long duration = demuxer.getDuration(); // 视频长度，微秒

        second = MathUtils.clamp(second, 0, duration / 1_000_000.0).doubleValue();

        long targetTimeMicros = (long) (second * 1_000_000);
        demuxer.seek(videoStreamIndex, targetTimeMicros, targetTimeMicros, duration, Demuxer.SeekFlag.SEEK_BYTE.swigValue());

        MediaPacket packet = MediaPacket.make();
        MediaPicture picture = MediaPicture.make(
                videoDecoder.getWidth(),
                videoDecoder.getHeight(),
                videoDecoder.getPixelFormat()
        );

        MediaPictureConverter converter;
        boolean found = false;

        while (demuxer.read(packet) >= 0 && !found) {

            if (packet.getStreamIndex() == videoStreamIndex) {
                int offset = 0;

                while (offset < packet.getSize()) {
                    int bytesDecoded = videoDecoder.decodeVideo(picture, packet, offset);
                    if (bytesDecoded < 0) break;
                    offset += bytesDecoded;

                    if (picture.isComplete()) {

                        DemuxerStream videoStream = demuxer.getStream(videoStreamIndex);
                        double timeBase = videoStream.getTimeBase().getDouble();
                        long pts = picture.getPts();
                        double frameSecond = pts * timeBase;

                        if (frameSecond >= second) {

                            converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
                            image = converter.toImage(null, picture);
                            found = true;
                            break;
                        }
                    }
                }
            }
        }

        // 清理资源
        demuxer.close();
        picture.delete();
        packet.delete();

        if (image == null)
            throw new RuntimeException("未找到图像");

        return image;
    }

    /**
     * 提取视频所有关键帧，返回 BufferedImage 列表，按关键帧时序排序。
     * <p>
     * 获取视频所有关键帧并保存为图片
     *
     * @param videoUrl   视频URL/路径
     * @param sampleRate 采样率 [0, 1]，决定会有多少百分比的关键帧被保留。过小的采样率对较短视频效果可能不佳。建议设在0.5以上。
     * @author Sijayyx
     */
    public static List<BufferedImage> extractKeyFramesAsBufferedImages(String videoUrl, double sampleRate) throws IOException, InterruptedException {
        if (sampleRate <= 0 || sampleRate > 1)
            throw new IllegalArgumentException("sampleRate must be in (0, 1]");

        List<BufferedImage> imageList = new ArrayList<>();

        Demuxer demuxer = Demuxer.make();
        demuxer.open(videoUrl, null, false, false, null, null);

        // 查找视频流并初始化解码器
        int videoStreamIndex = -1;
        Decoder videoDecoder = null;

        for (int i = 0; i < demuxer.getNumStreams(); i++) {
            DemuxerStream stream = demuxer.getStream(i);
            Decoder decoder = stream.getDecoder();
            if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                videoStreamIndex = i;
                videoDecoder = decoder;
                break;
            }
        }

        if (videoDecoder == null)
            throw new RuntimeException("无视频流!");

        videoDecoder.open(null, null);

        MediaPacket packet = MediaPacket.make();
        MediaPicture picture = MediaPicture.make(
                videoDecoder.getWidth(),
                videoDecoder.getHeight(),
                videoDecoder.getPixelFormat()
        );
        MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);

        int framesCount = 0;
        // 计算采样率
        int keepInterval = (int) (1.0 / sampleRate);  // 计算保留间隔

        while (demuxer.read(packet) >= 0) {
            if (packet.getStreamIndex() != videoStreamIndex) continue;
            if (!packet.isKey()) continue;

            int offset = 0;
            while (offset < packet.getSize()) {
                int bytesDecoded = videoDecoder.decode(picture, packet, offset);
                if (bytesDecoded < 0) break;
                offset += bytesDecoded;

                // 解码完成，转换为图片
                if (picture.isComplete()) {
                    framesCount++;

                    // 是否保留该帧？
                    boolean shouldKeep = (sampleRate >= 1.0) || (framesCount % keepInterval == 1);

                    if (shouldKeep) {
                        BufferedImage image = converter.toImage(null, picture);
                        imageList.add(image);

                        videoDecoder.flush();
                    }
                }
            }
        }

        videoDecoder.delete();
        demuxer.close();

        return imageList;
    }

    /**
     * 从输入流获取图片宽高
     * <p>
     * 请使用new出来的输入流，避免流被耗尽
     *
     * @return 图片[宽,高]
     */
    public static int[] getImageSize(InputStream inputStream) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            throw new IllegalArgumentException("不支持的图片格式或输入流无效");
        }

        inputStream.close();
        return new int[]{image.getWidth(), image.getHeight()};
    }

    /**
     * 获取Word图片类型
     *
     * @param img 图片路径/文件名
     * @return 图片类型
     */
    public static PictureType getPictureTypeForWord(String img) {
        String ext = img.substring(img.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "png" -> PictureType.PNG;
            case "gif" -> PictureType.GIF;
            default -> PictureType.JPEG;
        };
    }

    /**
     * 获取PPT图片类型
     *
     * @param img 图片路径/文件名
     * @return 图片类型
     */
    public static XSLFPictureData.PictureType getPictureTypeForPpt(String img) {
        String ext = img.substring(img.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "png" -> XSLFPictureData.PictureType.PNG;
            case "gif" -> XSLFPictureData.PictureType.GIF;
            default -> XSLFPictureData.PictureType.JPEG;
        };
    }

    /**
     * 将列表转换为附带索引的Json
     *
     * @param list 要格式化的列表
     * @return Json文本
     * @throws JsonProcessingException Json转换失败
     * @author Sijayyx
     */
    public static String listToIndexedJson(List<?> list) throws JsonProcessingException {

        List<Map<String, Object>> indexedList = IntStream.range(0, list.size())
                .mapToObj(i -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("index", i);
                    map.put("data", list.get(i));
                    return map;
                })
                .toList();
        return objectMapper.writeValueAsString(indexedList);
    }

    /**
     * 将Object转换为Json
     *
     * @param object 要转换的Object
     * @return Json文本
     * @throws JsonProcessingException Json转换失败
     * @author Sijayyx
     */
    public static String objectToJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 清除Yaml中的 ```yaml 与 ``` 字段
     *
     * @param yamlText 源Yaml文本
     * @return 处理后的文本
     * @author Sijayyx
     */
    public static String cleanYamlText(String yamlText) {
        return yamlText.replaceAll("```yaml", "").replaceAll("```", "");
    }

    /**
     * 将16进制颜色转换为Color对象
     *
     * @param hexStr 16进制颜色字符串
     * @return java.awt.Color对象
     * @author Sijayyx
     */
    public static Color hexToColor(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) {
            return Color.BLACK; // 默认黑色
        }

        // 移除前缀#（如果有）
        String hex = hexStr.startsWith("#") ? hexStr.substring(1) : hexStr;

        // 处理3位简写（#F00 → #FF0000）
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0)
                    + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2);
        }

        // 解析为16进制整数（RGB值）
        try {
            int rgb = Integer.parseInt(hex, 16);
            // 注意：Color(int rgb) 会自动将rgb转为ARGB，默认Alpha=255（不透明）
            return new Color(rgb);
        } catch (NumberFormatException e) {
            return Color.BLACK; // 解析失败返回默认色
        }
    }

}
