package com.superyoudi.businessservice.mapper;

import com.superyoudi.common.dto.ErrorQuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("select count(*) from task_student_rel where student_id = #{studentId}")
    Integer countStudentAllTask(Long studentId);

    List<ErrorQuestionVO> selectUserErrorQuestion(Long studentId, Long courseId);
}
