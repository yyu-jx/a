package com.superyoudi.businessservice.mapper;

import com.superyoudi.aiservice.pojo.entity.LearningPath;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LearningPathsMapper {

    /**
     * 根据学生id 查询【最近更新】的学习路径
     */
    @Select("SELECT id, student_id, course_id, updated_at " +
            "FROM learning_paths " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY updated_at DESC LIMIT 1")
    LearningPath selectLatestPathByStudentId(Long studentId);
}