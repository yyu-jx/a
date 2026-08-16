package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.vo.CourseVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    Integer countStudentJoinCourse(Long studentId, Long courseId);

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
     * 根据用户id查询用户角色
     *
     * @param userId 用户id
     * @return 用户角色
     */
    String selectUserRoleById(Long userId);

    /**
     * 根据教师id查询课程列表
     *
     * @param teacherId 教师id
     * @return 课程列表
     */
    List<CourseVO> selectTeacherCourseList(Long teacherId);

    /**
     * 根据学生id查询课程列表
     *
     * @param studentId 学生id
     * @return 课程列表
     */
    List<CourseVO> selectStudentCourseList(Long studentId);
}
