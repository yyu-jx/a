package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudyOverview;
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
    @Select("SELECT\n" +
            "  COUNT(*) AS study_course_total,\n" +
            "  AVG(total_progress) AS avg_total_progress,\n" +
            "  AVG(interaction_count) AS avg_interaction_count,\n" +
            "  AVG(avg_task_score) AS avg_task_score,\n" +
            "  AVG(knowledge_cover_rate) AS avg_knowledge_cover_rate,\n" +
            "  AVG(task_completion_rate) AS avg_task_completion_rate\n" +
            "FROM student_course_progresses\n" +
            "WHERE student_id = #{studentId};")
    StudyOverview getAvgProgresses(Long studentId);

    /**
     * 获取学生课程进度
     *
     * @param userId      用户ID
     * @param relateCourseIds 关联课程ID列表
     * @return 学生课程进度列表
     */
    List<StudentCourseProgress> selectProgressByStudentAndCourseIds(Long userId, List<Long> relateCourseIds);
}
