package com.superyoudi.businessservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CoursesMapper {

    /**
     * 根据课程id查询课程名称
     *
     * @param courseId 课程id
     * @return 课程名称
     */
    @Select("select name from courses where id = #{courseId}")
    String getNameById(Long courseId);
}
