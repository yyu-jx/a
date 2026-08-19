package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentSubmissionMapper {

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
