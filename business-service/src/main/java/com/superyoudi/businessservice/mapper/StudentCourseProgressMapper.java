package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.common.dto.StudentCourseProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentCourseProgressMapper {

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
