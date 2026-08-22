package com.superyoudi.businessservice.mapper;


import com.superyoudi.businessservice.pojo.entity.StudentCourseKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseStudentRelMapper {

    @Select("""
            SELECT cs.student_id AS studentId, cs.course_id AS courseId
            FROM course_student_rel cs
            JOIN courses c ON c.id = cs.course_id
            WHERE c.status = 'opening'
            """)
    List<StudentCourseKey> selectActiveStudentCourses();

    @Select("SELECT COUNT(*) FROM course_student_rel WHERE course_id = #{courseId}")
    Long countStudentsByCourseId(@Param("courseId") Long courseId);
}