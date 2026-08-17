package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.TaskQuestion;
import com.superyoudi.common.dto.QuestionDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    /**
     * 插入题目
     */
    @Insert("INSERT INTO questions (course_id, teacher_id, type, content, answer) " +
            "VALUES (#{courseId}, #{teacherId}, #{type}, #{content}, #{answer})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(Question question);

    /**
     * 根据ID删除题目
     */
    @Delete("DELETE FROM questions WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据ID更新题目
     */
    @Update("UPDATE questions SET " +
            "course_id = #{courseId}, " +
            "teacher_id = #{teacherId}, " +
            "type = #{type}, " +
            "content = #{content}, " +
            "answer = #{answer} " +
            "WHERE id = #{id}")
    Long update(Question question);

    /**
     * 根据ID查询题目
     */
    @Select("SELECT * FROM questions WHERE id = #{id}")
    Question findById(Long id);

    /**
     * 查询所有题目
     */
    @Select("SELECT * FROM questions")
    List<Question> findAll();

    /**
     * 根据课程ID查询题目列表
     */
    @Select("SELECT * FROM questions WHERE course_id = #{courseId}")
    List<Question> findByCourseId(Long courseId);

    /**
     * 根据教师ID查询题目列表
     */
    @Select("SELECT * FROM questions WHERE teacher_id = #{teacherId}")
    List<Question> findByTeacherId(Long teacherId);

    /**
     * 根据课程ID和题型查询题目列表
     */
    @Select("SELECT * FROM questions WHERE course_id = #{courseId} AND type = #{type}")
    List<Question> findByCourseIdAndType(@Param("courseId") Long courseId,
                                         @Param("type") String type);

    /**
     * 根据教师ID和题型查询题目列表
     */
    @Select("SELECT * FROM questions WHERE teacher_id = #{teacherId} AND type = #{type}")
    List<Question> findByTeacherIdAndType(@Param("teacherId") Long teacherId,
                                          @Param("type") String type);

    /**
     * 根据教师ID和课程ID查询题目列表
     */
    @Select("SELECT * FROM questions WHERE teacher_id = #{teacherId} AND course_id = #{courseId}")
    List<Question> findByTeacherIdAndCourseId(@Param("teacherId") Long teacherId,
                                              @Param("courseId") Long courseId);

    /**
     * 根据教师ID、课程ID和题型查询题目列表
     */
    @Select("SELECT * FROM questions WHERE teacher_id = #{teacherId} AND course_id = #{courseId} AND type = #{type}")
    List<Question> findByTeacherIdAndCourseIdAndType(@Param("teacherId") Long teacherId,
                                                     @Param("courseId") Long courseId,
                                                     @Param("type") String type);

    /**
     * 根据课程ID删除题目
     */
    @Delete("DELETE FROM questions WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);

    /**
     * 根据教师ID删除题目
     */
    @Delete("DELETE FROM questions WHERE teacher_id = #{teacherId}")
    Long deleteByTeacherId(Long teacherId);

    /**
     * 统计课程下的题目数量
     */
    @Select("SELECT COUNT(*) FROM questions WHERE course_id = #{courseId}")
    Long countByCourseId(Long courseId);

    /**
     * 统计教师下的题目数量
     */
    @Select("SELECT COUNT(*) FROM questions WHERE teacher_id = #{teacherId}")
    Long countByTeacherId(Long teacherId);

    /**
     * 统计课程下各题型的数量
     */
    @Select("SELECT type, COUNT(*) as count FROM questions " +
            "WHERE course_id = #{courseId} GROUP BY type")
    List<java.util.Map<String, Object>> countByTypeForCourse(Long courseId);

    /**
     * 分页查询课程题目
     */
    @Select("SELECT * FROM questions WHERE course_id = #{courseId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Question> findQuestionsByCourseWithPage(@Param("courseId") Long courseId,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    @Select("select id, content,  answer from questions where id = #{questionId}")
    QuestionDTO selectById(Long questionId);

    /**
     * 根据任务ID查询题目
     */
    List<TaskQuestion> selectByTaskId(Long taskId);
}
