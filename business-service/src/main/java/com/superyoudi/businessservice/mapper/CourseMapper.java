package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT course_data_id FROM courses WHERE id = #{courseId}")
    Long getCourseDataId(Long courseId);

    /**
     * 根据学生id和课程id查询学生是否已加入该课程
     *
     * @param studentId 学生id
     * @param courseId  课程id
     * @return 1:已加入，0:未加入
     */
    @Select("SELECT COUNT(*) FROM course_student_rel WHERE student_id = #{studentId} AND course_id = #{courseId}")
    Integer countStudentJoinCourse(Long studentId, Long courseId);

    /**
     * 统计指定教师的所有课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId}")
    Integer countCoursesByTeacherId(Long teacherId);

    /**
     * 统计指定教师的所有课程的学生总数
     */
    @Select("SELECT COUNT(DISTINCT cs.student_id) " +
            "FROM courses c " +
            "INNER JOIN course_student_rel cs ON c.id = cs.course_id " +
            "WHERE c.teacher_id = #{teacherId}")
    Integer countStudentsByTeacherId(Long teacherId);

    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId}")
    List<Course> selectByTeacherId(Long teacherId);

    /**
     * 根据课程id查询课程名称
     *
     * @param courseId 课程id
     * @return 课程名称
     */
    @Select("select name from courses where id = #{courseId}")
    String getNameById(Long courseId);

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


    /**
     * 插入课程
     */
    @Insert("INSERT INTO courses (name, course_data_id, description, cover_url, credit, status, teacher_id) " +
            "VALUES (#{name}, #{courseDataId}, #{description}, #{coverUrl}, #{credit}, #{status}, #{teacherId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);


    /**
     * 查询所有课程
     */
    @Select("SELECT * FROM courses ORDER BY created_at DESC")
    List<Course> selectAll();


    /**
     * 根据状态查询课程列表
     */
    @Select("SELECT * FROM courses WHERE status = #{status} ORDER BY created_at DESC")
    List<Course> selectByStatus(@Param("status") String status);

    /**
     * 根据教师ID和状态查询课程列表
     */
    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId} AND status = #{status} ORDER BY created_at DESC")
    List<Course> selectByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") String status);

    /**
     * 根据名称模糊查询课程
     */
    @Select("SELECT * FROM courses WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY created_at DESC")
    List<Course> selectByNameLike(@Param("name") String name);

    /**
     * 更新课程状态
     */
    @Update("UPDATE courses SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据ID删除课程
     */
    @Delete("DELETE FROM courses WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据教师ID删除所有课程
     */
    @Delete("DELETE FROM courses WHERE teacher_id = #{teacherId}")
    int deleteByTeacherId(Long teacherId);

    /**
     * 统计教师课程总数
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId}")
    Long countByTeacherId(Long teacherId);

    /**
     * 统计教师正在上课的课程总数
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId} AND status = 'opening'")
    Long countOpeningByTeacherId(Long teacherId);
}
