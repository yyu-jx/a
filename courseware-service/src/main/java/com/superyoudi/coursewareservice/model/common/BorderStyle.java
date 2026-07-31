package com.superyoudi.coursewareservice.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 边框样式
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorderStyle {
    /// 边框宽度 px
    private Double width;
    /// solid, dashed, dotted
    private String color;
    /// #RRBBGG
    private StyleType style;

    public enum StyleType {
        SOLID("SOLID"), DASHED("DASH"), DOTTED("DOT");

        public final String style;

        StyleType(String style) {
            this.style = style;
        }

        public static StyleType fromString(String value) {
            if (value == null) {
                return null;
            }
            for (StyleType type : values()) {
                if (type.style.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant StyleType with value: " + value);
        }

        public String getStyleName() {
            return style;
        }
    }

}
