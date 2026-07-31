package com.superyoudi.coursewareservice.model.ppt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.superyoudi.coursewareservice.utils.CommonUtils.objectToDouble;

/**
 * 视频元素数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoData {
    /// 视频的 URL （base64或http url）
    private String vidUrl;
    /// 视频封面的 URL
    private String coverUrl;
    /// 视频时长，秒
    private Double duration;

    public static VideoData fromObjectMap(Map<String, Object> map) {
        return new VideoData(
                (String) map.get("vidUrl"),
                (String) map.get("coverUrl"),
                objectToDouble(map.get("duration"))
        );
    }
}
