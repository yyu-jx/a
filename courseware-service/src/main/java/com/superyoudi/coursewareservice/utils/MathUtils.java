package com.superyoudi.coursewareservice.utils;


/**
 * 数学工具类，包括计算尺寸、按比例计算高度等
 *
 * @author Sijayyx
 */
public class MathUtils {

    /**
     * 将磅转换为像素
     *
     * @param pt 磅数
     * @return 像素数
     */
    public static int pointToPixel(double pt) {
        return (int) Math.round(pt * 96.0 / 72.0);
    }

    /**
     * 限制数字在指定范围内
     *
     * @param value 数字
     * @param min   最小值
     * @param max   最大值
     * @return 限制后的数字
     */
    public static Number clamp(Number value, Number min, Number max) {
        if (value.doubleValue() < min.doubleValue()) {
            return min;
        } else if (value.doubleValue() > max.doubleValue()) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * 调整尺寸，保持宽高比
     *
     * @param width   原始宽度
     * @param height  原始高度
     * @param maxSize 最大尺寸
     * @param minSize 最小尺寸
     * @return 调整后的尺寸（包含宽和高）
     */
    public static int[] resizeWithAspectRatio(int width, int height, int maxSize, int minSize) {
        // 防止除零异常
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("宽度和高度必须大于0");
        }

        // 计算宽高比
        double aspectRatio = (double) width / height;

        int newWidth = width;
        int newHeight = height;

        // 处理超过最大值的情况
        if (newWidth > maxSize || newHeight > maxSize) {
            if (newWidth > maxSize) {
                newWidth = maxSize;
                newHeight = (int) Math.round(newWidth / aspectRatio);
            }
            // 检查调整后的高度是否超出最大值
            if (newHeight > maxSize) {
                newHeight = maxSize;
                newWidth = (int) Math.round(newHeight * aspectRatio);
            }
        }

        // 处理小于最小值的情况
        if (newWidth < minSize || newHeight < minSize) {
            if (newWidth < minSize) {
                newWidth = minSize;
                newHeight = (int) Math.round(newWidth / aspectRatio);
            }
            // 检查调整后的高度是否小于最小值
            if (newHeight < minSize) {
                newHeight = minSize;
                newWidth = (int) Math.round(newHeight * aspectRatio);
            }
        }

        // 最终确保值在范围内（防止浮点计算误差）
        newWidth = Math.max(minSize, Math.min(maxSize, newWidth));
        newHeight = Math.max(minSize, Math.min(maxSize, newHeight));

        return new int[]{newWidth, newHeight};
    }

    /**
     * 固定宽度，按比例计算高度
     *
     * @param width      原始宽度
     * @param height     原始高度
     * @param fixedWidth 目标宽度
     * @return 目标高度（保持宽高比）
     *
     */
    public static float calcHeightWithFixedWidth(float width, float height, float fixedWidth) {
        // 按宽度比例计算高度
        return fixedWidth / width * height;
    }

//    /**
//     * 计算最接近的分辨率枚举
//     *
//     * @param width  原始宽度
//     * @param height 原始高度
//     * @return 最接近的分辨率枚举
//     */
//    public static VideoResolutionType calcNearestResolution(int width, int height) {
//        VideoResolutionType closest = VideoResolutionType.FREE;
//        double minDistance = Double.MAX_VALUE;
//
//        for (VideoResolutionType res : VideoResolutionType.values()) {
//            // 计算宽高比的差异
//            double targetRatio = (double) width / height;
//            double resRatio = (double) res.getWidth() / res.getHeight();
//            double ratioDiff = Math.abs(targetRatio - resRatio);
//
//            // 计算面积的差异
//            double targetArea = width * height;
//            double resArea = res.getWidth() * res.getHeight();
//            double areaDiff = Math.abs(targetArea - resArea) / Math.max(targetArea, resArea);
//
//            // 综合距离，乘上各自的权重 (比例和大小哪个更重要)
//            // 我觉得比例更重要一些,,,
//            double distance = ratioDiff * 0.9 + areaDiff * 0.1;
//
//            if (distance < minDistance) {
//                minDistance = distance;
//                closest = res;
//            }
//        }
//
//        return closest;
//    }
}
