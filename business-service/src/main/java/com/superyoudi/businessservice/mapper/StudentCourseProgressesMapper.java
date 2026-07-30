package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.vo.StudyOverviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;

@Mapper
public interface StudentCourseProgressesMapper {


    /**
     * 获取学习概览
     *
     * @param userId 用户ID
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
    StudyOverviewVO getAvgProgresses(Integer userId);
}
