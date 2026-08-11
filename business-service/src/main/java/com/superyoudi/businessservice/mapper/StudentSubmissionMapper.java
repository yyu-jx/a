package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface StudentSubmissionMapper {

    @Select("select * from student_submissions where task_id = #{taskId} and student_id = #{student_id} limit 1")
    StudentSubmission selectByTaskAndStudentId(Long taskId, Long student_id);

    @Update("update student_submissions " +
            "set status = #{status}, submitted_time = #{submittedTime} " +
            "where student_id = #{studentId} and task_id = #{taskId}")
    int updateById(StudentSubmission record);

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
