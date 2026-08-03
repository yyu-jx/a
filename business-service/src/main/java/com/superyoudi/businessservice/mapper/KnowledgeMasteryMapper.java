package com.superyoudi.businessservice.mapper;

import com.superyoudi.common.dto.StudentKnowledgeMastery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeMasteryMapper {

    @Select("SELECT knowledge_id,course_id, question_correct_count, question_total_count, " +
            "ROUND(100 - (question_correct_count / question_total_count * 100), 2) AS error_rate " +
            "FROM student_knowledge_masteries " +
            "WHERE student_id = #{userId} AND question_total_count > 0 " +
            "ORDER BY error_rate DESC LIMIT 3")
     List<StudentKnowledgeMastery> selectTop3WeakKnowledge(Long userId) ;

    /**
     * 查询学生TOP5薄弱知识点
     * @param userId 用户id
     * @return 知识点掌握数据集合
     */
    @Select("SELECT knowledge_id, question_correct_count, question_total_count, " +
            "ROUND(100 - (question_correct_count / question_total_count * 100), 2) AS error_rate " +
            "FROM student_knowledge_masteries " +
            "WHERE student_id = #{userId} AND question_total_count > 0 " +
            "ORDER BY error_rate DESC LIMIT 5")
    List<StudentKnowledgeMastery> selectTop5WeakKnowledge(Long userId);
}
