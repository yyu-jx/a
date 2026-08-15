package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.CourseData;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface CourseDataMapper {

    @Insert("INSERT INTO course_data (name, info) VALUES (#{name}, #{info})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(CourseData courseData);

    @Select("SELECT * FROM course_data WHERE id = #{id}")
    CourseData selectById(Long id);

    @Select("SELECT * FROM course_data ORDER BY created_at DESC")
    List<CourseData> selectAll();

    @Select("SELECT * FROM course_data WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY created_at DESC")
    List<CourseData> selectByNameLike(@Param("name") String name);

    @Delete("DELETE FROM course_data WHERE id = #{id}")
    Long deleteById(Long id);

    @Select("SELECT COUNT(*) FROM course_data WHERE id = #{id}")
    int existsById(Long id);
}