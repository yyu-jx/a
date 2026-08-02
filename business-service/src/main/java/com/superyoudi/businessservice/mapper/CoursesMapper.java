package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Courses;
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


    @Select("SELECT c.id course_id, c.name, c.description, c.cover_url cover_url, c.credit, c.status, " +
            "c.teacher_id, u.realname teacher_name, c.created_at, c.updated_at " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.id = #{courseId}")
    Courses selectCourseDetailById(Long courseId);
}
