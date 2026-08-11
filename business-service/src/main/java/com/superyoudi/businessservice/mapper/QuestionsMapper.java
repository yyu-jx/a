package com.superyoudi.businessservice.mapper;

import com.superyoudi.common.dto.QuestionsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionsMapper {

    @Select("select id, content,  answer from questions where id = #{questionId}")
    QuestionsDTO selectById(Long questionId);
}
