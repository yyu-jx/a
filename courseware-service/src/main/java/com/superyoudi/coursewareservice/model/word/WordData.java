package com.superyoudi.coursewareservice.model.word;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordData {
    private String wordName;
    private List<WordElement> wordElements;
}
