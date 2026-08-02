package com.superyoudi.coursewareservice.model.ppt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.geom.Rectangle2D;

/**
 * 元素的矩形坐标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElementRect {

    /// 元素的X坐标，%
    private Double x;
    /// 元素的Y坐标，%
    private Double y;
    /// 元素的宽度，%
    private Double width;
    /// 元素的高度，%
    private Double height;


    /// 根据基础坐标，转换成绝对坐标
    public Rectangle2D toRelativeRect2D(double baseX, double baseY) {
        double actualX = baseX * (x / 100.0);
        double actualY = baseY * (y / 100.0);
        double actualWidth = baseX * (width / 100.0);
        double actualHeight = baseY * (height / 100.0);

        return new Rectangle2D.Double(actualX, actualY, actualWidth, actualHeight);
    }
}
