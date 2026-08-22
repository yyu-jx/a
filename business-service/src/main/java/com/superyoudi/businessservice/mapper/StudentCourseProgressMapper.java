package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.common.dto.StudentCourseProgress;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface StudentCourseProgressMapper {

    @Select("SELECT COALESCE(AVG(total_progress), 0) FROM student_course_progresses WHERE course_id = #{courseId}")
    Double selectAvgProgressByCourseId(@Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(*)
            FROM student_course_progresses
            WHERE course_id = #{courseId}
              AND (total_progress < 60 OR task_completion_rate < 60)
            """)
    Long countAlertStudentsByCourseId(@Param("courseId") Long courseId);

    @Insert("""
            INSERT INTO student_course_progresses
              (student_id, course_id, total_progress, interaction_count, avg_task_score,
               knowledge_cover_rate, task_completion_rate, updated_at)
            VALUES (#{studentId}, #{courseId}, #{totalProgress}, #{interactionCount}, #{avgTaskScore},
                    #{knowledgeCoverRate}, #{taskCompletionRate}, NOW())
            ON DUPLICATE KEY UPDATE
              total_progress = #{totalProgress},
              interaction_count = #{interactionCount},
              avg_task_score = #{avgTaskScore},
              knowledge_cover_rate = #{knowledgeCoverRate},
              task_completion_rate = #{taskCompletionRate},
              updated_at = NOW()
            """)
    int upsert(StudentCourseProgress progress);

    /**
     * 获取学习概览
     *
     * @param studentId 学生ID
     * @return 学习概览
     */
    @Select("""
            SELECT
              COUNT(*) AS study_course_total,
              AVG(total_progress) AS avg_total_progress,
              AVG(interaction_count) AS avg_interaction_count,
              AVG(avg_task_score) AS avg_task_score,
              AVG(knowledge_cover_rate) AS avg_knowledge_cover_rate,
              AVG(task_completion_rate) AS avg_task_completion_rate
            FROM student_course_progresses
            WHERE student_id = #{studentId};""")
    StudentStudyOverview getAvgProgresses(Long studentId);

    /**
     * 获取学生课程进度
     *
     * @param userId    用户ID
     * @param CourseIds 关联课程ID列表
     * @return 学生课程进度列表
     */
    List<StudentCourseProgress> selectProgressByStudentAndCourseIds(Long userId, List<Long> CourseIds);

    /**
     * 获取学生任务均分
     *
     * @param studentId    用户ID
     * @return 学生任务均分
     */
    @Select("select AVG(avg_task_score) from student_course_progresses where student_id = #{studentId}")
    Double getAvgTaskScore(Long studentId);
}
