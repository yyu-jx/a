package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.TaskScoreStats;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentSubmissionMapper {

    @Select("""
            SELECT COALESCE(AVG(s.total_score * 100.0 / NULLIF(t.total_score, 0)), 0)
            FROM student_submissions s
            JOIN tasks t ON t.id = s.task_id
            WHERE t.course_id = #{courseId}
              AND t.status IN ('published','closed')
              AND s.status IN ('passed','returned')
            """)
    Double selectAvgScoreRateByCourseId(@Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(*)
            FROM student_submissions s
            JOIN tasks t ON t.id = s.task_id
            WHERE t.course_id = #{courseId}
              AND t.status IN ('published','closed')
              AND s.status IN ('submitted','passed','returned')
            """)
    Long countSubmittedByCourseId(@Param("courseId") Long courseId);

    @Select("""
            SELECT COALESCE(AVG(s.total_score), 0)
            FROM student_submissions s
            JOIN tasks t ON t.id = s.task_id
            WHERE s.student_id = #{studentId} AND t.course_id = #{courseId}
              AND s.status IN ('passed','returned')
            """)
    Double selectAvgTaskScore(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(*)
            FROM student_submissions s
            JOIN tasks t ON t.id = s.task_id
            WHERE s.student_id = #{studentId} AND t.course_id = #{courseId}
              AND s.status IN ('submitted','passed','returned')
            """)
    Long countCompletedTasks(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(*)
            FROM student_submissions
            WHERE task_id = #{taskId} AND status IN ('submitted','passed','returned')
            """)
    Integer countSubmittedByTaskId(@Param("taskId") Long taskId);

    @Select("""
            SELECT COALESCE(AVG(total_score), 0) AS avgScore,
                   COALESCE(MAX(total_score), 0) AS maxScore,
                   COALESCE(MIN(total_score), 0) AS minScore
            FROM student_submissions
            WHERE task_id = #{taskId} AND status IN ('passed','returned')
            """)
    TaskScoreStats selectScoreStatsByTaskId(@Param("taskId") Long taskId);

    @Select("select * from student_submissions where task_id = #{taskId} and student_id = #{student_id} limit 1")
    StudentSubmission selectByTaskAndStudentId(Long taskId, Long studentId);


    @Select("select * from student_submissions where task_id = #{taskId}")
    List<StudentSubmission> selectByTaskId(Long taskId);

    @Select("select * from student_submissions")
    List<StudentSubmission> selectAll();


    /**
     * 根据ID更新提交记录
     */
    @Update("UPDATE student_submissions SET " +
            "task_id = #{taskId}, " +
            "student_id = #{studentId}, " +
            "status = #{status}, " +
            "submitted_time = #{submittedTime}, " +
            "graded_time = #{gradedTime}, " +
            "total_score = #{totalScore}, " +
            "comment = #{comment} " +
            "WHERE id = #{id}")
    Long update(StudentSubmission submission);

    @Insert("insert into student_submissions(task_id, student_id, status, submitted_time, created_at) " +
            "values(#{taskId}, #{studentId}, #{status}, #{submittedTime}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentSubmission newRecord);


    @Select("SELECT count(*) FROM student_submissions " +
            "WHERE student_id = #{studentId} AND status IN ('passed','proceeding')")
    Integer countFinishedTask(Long studentId);


    @Select("SELECT count(*) FROM student_submissions WHERE student_id = #{studentId} AND status = 'draft'")
    Integer countDraftTask(Long userId);
}
