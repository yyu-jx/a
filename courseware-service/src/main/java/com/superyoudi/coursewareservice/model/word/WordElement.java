package com.superyoudi.coursewareservice.model.word;

import com.superyoudi.coursewareservice.model.common.ElementType;
import com.superyoudi.coursewareservice.model.common.TableData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Word元素
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class WordElement {
    private ElementType type;
    private Object elem;

    @SuppressWarnings("unchecked")
    public WordParagraph asWordParagraph() {
        Map<String, Object> map = (Map<String, Object>) elem;
        return WordParagraph.fromObjectMap(map);
    }

    @SuppressWarnings("unchecked")
    public WordImage asWordImage() {
        Map<String, Object> map = (Map<String, Object>) elem;
        return WordImage.fromObjectMap(map);
    }

    @SuppressWarnings("unchecked")
    public TableData asTableData() {
        Map<String, Object> map = (Map<String, Object>) elem;
        return TableData.fromObjectMap(map);
    }
}
