package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
}
