package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.TeacherCourseAnalysis;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface TeacherCourseAnalysisMapper {

    @Insert("""
            INSERT INTO teacher_course_analysises
              (course_id, total_students, avg_score_rate, avg_progress, task_count,
               submission_rate, alert_student_count, weakest_knowledge, updated_at)
            VALUES (#{courseId}, #{totalStudents}, #{avgScoreRate}, #{avgProgress}, #{taskCount},
                    #{submissionRate}, #{alertStudentCount}, #{weakestKnowledge}, NOW())
            ON DUPLICATE KEY UPDATE
              total_students = #{totalStudents},
              avg_score_rate = #{avgScoreRate},
              avg_progress = #{avgProgress},
              task_count = #{taskCount},
              submission_rate = #{submissionRate},
              alert_student_count = #{alertStudentCount},
              weakest_knowledge = #{weakestKnowledge},
              updated_at = NOW()
            """)
    int upsert(TeacherCourseAnalysis analysis);

    /**
     * 插入一条记录
     */
    @Insert("""
            INSERT INTO teacher_course_analysises (course_id, total_students, avg_score_rate, avg_progress, \
            task_count, submission_rate, opening_questions, alert_student_count, weakest_knowledge) \
            VALUES (#{courseId}, #{totalStudents}, #{avgScoreRate}, #{avgProgress}, #{taskCount}, \
            #{submissionRate}, #{openingQuestions}, #{alertStudentCount}, #{weakestKnowledge})""")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(TeacherCourseAnalysis analysis);

    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM teacher_course_analysises WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据课程ID删除
     */
    @Delete("DELETE FROM teacher_course_analysises WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);

    /**
     * 更新记录
     */
    @Update("""
            UPDATE teacher_course_analysises SET \
            total_students = #{totalStudents}, \
            avg_score_rate = #{avgScoreRate}, \
            avg_progress = #{avgProgress}, \
            task_count = #{taskCount}, \
            submission_rate = #{submissionRate}, \
            opening_questions = #{openingQuestions}, \
            alert_student_count = #{alertStudentCount}, \
            weakest_knowledge = #{weakestKnowledge} \
            WHERE id = #{id}""")
    Long update(TeacherCourseAnalysis analysis);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM teacher_course_analysises WHERE id = #{id}")
    TeacherCourseAnalysis selectById(Long id);

    /**
     * 根据课程ID查询
     */
    @Select("SELECT * FROM teacher_course_analysises WHERE course_id = #{courseId}")
    TeacherCourseAnalysis selectByCourseId(Long courseId);

    /**
     * 查询所有记录
     */
    @Select("SELECT * FROM teacher_course_analysises")
    List<TeacherCourseAnalysis> selectAll();

    /**
     * 查询所有预警学生数量大于0的记录
     */
    @Select("SELECT * FROM teacher_course_analysises WHERE alert_student_count > 0")
    List<TeacherCourseAnalysis> selectWithAlert();

    /**
     * 更新平均得分率
     */
    @Update("UPDATE teacher_course_analysises SET avg_score_rate = #{avgScoreRate} WHERE course_id = #{courseId}")
    Long updateAvgScoreRate(@Param("courseId") Long courseId, @Param("avgScoreRate") BigDecimal avgScoreRate);

    /**
     * 获取任教课程的总平均进度
     */
    @Select("""
            SELECT AVG(avg_score_rate)
            FROM teacher_course_analysises tca
            INNER JOIN courses c ON tca.course_id = c.id
            WHERE c.teacher_id = #{teacherId}
            """)
    Double getTotalAvgProgress(Long teacherId);

    /**
     * 统计总记录数
     */
    @Select("SELECT COUNT(*) FROM teacher_course_analysises")
    long count();



}