package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Course;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseMapper {

    /**
     * 根据学生id和课程id查询学生是否已加入该课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     * @return 1:已加入，0:未加入
     */
    @Select("SELECT COUNT(*) FROM course_student_rel WHERE student_id = #{studentId} AND course_id = #{courseId}")
     Integer countStudentJoinCourse(Long studentId, Long courseId) ;

    /**
     * 根据课程id查询课程名称
     *
     * @param courseId 课程id
     * @return 课程名称
     */
    @Select("select name from courses where id = #{courseId}")
    String getNameById(Long courseId);

    /**
     * 根据课程id查询课程详情
     *
     * @param courseId 课程id
     * @return 课程详情
     */
    @Select("SELECT c.id course_id, c.name, c.description, c.cover_url cover_url, c.credit, c.status, " +
            "c.teacher_id, u.realname teacher_name, c.created_at, c.updated_at " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "WHERE c.id = #{courseId}")
    Course selectCourseById(Long courseId);

    /**
     * 加入课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     */
    @Insert("INSERT INTO course_student_rel (student_id, course_id) VALUES (#{studentId}, #{courseId})")
    void joinCourse(Long studentId, Long courseId);

    /**
     * 根据课程id查询课程信息
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Select("SELECT * FROM courses WHERE id = #{courseId}")
    Course selectById(Long courseId);
}
