package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentAnswerMapper {
    /**
     * 插入学生答案
     */
    @Insert("INSERT INTO student_answers (submission_id, question_id, answer, score, comment, " +
            "time_spent, attempt_count, help_request_count, error_type) " +
            "VALUES (#{submissionId}, #{questionId}, #{answer}, #{score}, #{comment}, " +
            "#{timeSpent}, #{attemptCount}, #{helpRequestCount}, #{errorType})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(StudentAnswer studentAnswer);

    @Select("select id, help_request_count from student_answers " +
            "where submission_id = #{submissionId} and question_id = #{questionId} limit 1")
    StudentAnswer selectAnswerBySubQid(Long submissionId, Long questionId);

    @Update("update student_answers set help_request_count = help_request_count + 1 where id = #{id}")
    void incrementHelpCount(Long id);

    @Insert("insert into student_answers(submission_id, question_id, help_request_count) " +
            "values(#{submissionId},#{questionId},#{helpRequestCount})")
    void insertEmptyAnswerRecord(Long submissionId, Long questionId, Long i);


    @Select("select * from student_answers where id = #{id}")
    StudentAnswer selectById(Long id);

    /**
     * 根据学生ID和提交ID查询该提交下的所有答案
     *
     * @param studentId    学生ID
     * @param submissionId 提交ID
     * @return 该提交下的所有答案列表
     */
    @Select("SELECT sa.* FROM student_answers sa " +
            "INNER JOIN student_submissions ss ON sa.submission_id = ss.id " +
            "WHERE ss.student_id = #{studentId} AND ss.id = #{submissionId}")
    List<StudentAnswer> selectByStudentIdAndSubmissionId(@Param("studentId") Long studentId,
                                                         @Param("submissionId") Long submissionId);

    @Select("select * from student_answers")
    List<StudentAnswer> selectAll();

    /**
     * 计算提交的总得分（单题得分之和）
     *
     * @param submissionId 提交ID
     * @return 总得分，如果没有记录则返回0
     */
    @Select("SELECT COALESCE(SUM(score), 0) FROM student_answers WHERE submission_id = #{submissionId}")
    Integer sumScoreBySubmissionId(Long submissionId);

    @Update("update student_answers set answer = #{answer}, score = #{score}, comment = #{comment}, " +
            "time_spent = #{timeSpent}, attempt_count = #{attemptCount}, " +
            "help_request_count = #{helpRequestCount}, error_type = #{errorType} " +
            "where id = #{id}")
    int update(StudentAnswer answer);
}
