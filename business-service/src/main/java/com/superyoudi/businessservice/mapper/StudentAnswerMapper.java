package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface StudentAnswerMapper {

    @Select("select id, help_request_count from student_answers " +
            "where submission_id = #{submissionId} and question_id = #{questionId} limit 1")
    StudentAnswer selectAnswerBySubQid(Long submissionId, Long questionId);

    @Update("update student_answers set help_request_count = help_request_count + 1 where id = #{id}")
    void incrementHelpCount(Long id);

    @Insert("insert into student_answers(submission_id, question_id, help_request_count) " +
            "values(#{submissionId},#{questionId},#{helpRequestCount})")
    void insertEmptyAnswerRecord(Long submissionId, Long questionId, Long i);

    /* ── 批改用 ── */

    @Select("select * from student_answers where id = #{id}")
    StudentAnswer selectById(Long id);

    @Select("select * from student_answers where submission_id = #{submissionId}")
    List<StudentAnswer> selectBySubmissionId(Long submissionId);

    @Select("select sa.* from student_answers sa " +
            "join student_submissions ss on sa.submission_id = ss.id " +
            "where ss.student_id = #{studentId}")
    List<StudentAnswer> selectByStudentId(Long studentId);

    @Select("select * from student_answers")
    List<StudentAnswer> selectAll();

    @Update("update student_answers set answer = #{answer}, score = #{score}, comment = #{comment}, " +
            "time_spent = #{timeSpent}, attempt_count = #{attemptCount}, " +
            "help_request_count = #{helpRequestCount}, error_type = #{errorType} " +
            "where id = #{id}")
    int updateById(StudentAnswer answer);
}
