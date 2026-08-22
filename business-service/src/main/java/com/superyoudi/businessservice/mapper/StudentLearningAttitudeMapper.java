package com.superyoudi.businessservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentLearningAttitudeMapper {

    @Select("SELECT COALESCE(SUM(interaction_count), 0) FROM student_learning_attitudes WHERE student_id = #{studentId}")
    Long sumInteractionCount(@Param("studentId") Long studentId);
}