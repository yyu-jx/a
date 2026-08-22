package com.superyoudi.businessservice.mapper;

import com.superyoudi.common.dto.StudentKnowledgeMastery;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentKnowledgeMasteryMapper {

//    @Select("SELECT knowledge_id,course_id, question_correct_count, question_total_count, " +
//            "ROUND(100 - (question_correct_count / question_total_count * 100), 2) AS error_rate " +
//            "FROM student_knowledge_masteries " +
//            "WHERE student_id = #{userId} AND question_total_count > 0 " +
//            "ORDER BY error_rate DESC LIMIT 3")
//    List<StudentKnowledgeMastery> selectTop3WeakKnowledge(Long userId);
//
//    /**
//     * 查询学生TOP5薄弱知识点
//     *
//     * @param userId 用户id
//     * @return 知识点掌握数据集合
//     */
//    @Select("SELECT knowledge_id, question_correct_count, question_total_count, " +
//            "ROUND(100 - (question_correct_count / question_total_count * 100), 2) AS error_rate " +
//            "FROM student_knowledge_masteries " +
//            "WHERE student_id = #{userId} AND question_total_count > 0 " +
//            "ORDER BY error_rate DESC LIMIT 5")
//    List<StudentKnowledgeMastery> selectTop5WeakKnowledge(Long userId);

    @Select("""
            SELECT knowledge_id
            FROM student_knowledge_masteries
            WHERE course_id = #{courseId}
            ORDER BY master_rate ASC, updated_at ASC
            LIMIT 1
            """)
    String selectWeakestKnowledgeByCourseId(@Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(DISTINCT knowledge_id)
            FROM student_knowledge_masteries
            WHERE student_id = #{studentId} AND course_id = #{courseId}
            """)
    Long countCoveredKnowledge(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("SELECT * FROM student_knowledge_masteries WHERE id = #{id}")
    StudentKnowledgeMastery selectById(Long id);

    /**
     * 根据学生ID和知识点ID查询（唯一记录）
     */
    @Select("SELECT * FROM student_knowledge_masteries WHERE student_id = #{studentId} AND knowledge_id = #{knowledgeId}")
    StudentKnowledgeMastery selectByStudentAndKnowledge(@Param("studentId") Long studentId, @Param("knowledgeId") String knowledgeId);

    /**
     * 根据学生ID和课程ID查询所有知识点掌握情况
     */
    @Select("SELECT * FROM student_knowledge_masteries WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<StudentKnowledgeMastery> selectByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据学生ID查询所有知识点掌握情况
     */
    @Select("SELECT * FROM student_knowledge_masteries WHERE student_id = #{studentId}")
    List<StudentKnowledgeMastery> selectByStudentId(Long studentId);

    /**
     * 根据课程ID查询所有学生的知识点掌握情况
     */
    @Select("SELECT * FROM student_knowledge_masteries WHERE course_id = #{courseId}")
    List<StudentKnowledgeMastery> selectByCourseId(Long courseId);

    /**
     * 根据知识点ID查询所有学生的掌握情况
     */
    @Select("SELECT * FROM student_knowledge_masteries WHERE knowledge_id = #{knowledgeId}")
    List<StudentKnowledgeMastery> selectByKnowledgeId(String knowledgeId);

    /**
     * 查询学生某课程下掌握率低于指定阈值的知识点
     */
    @Select("SELECT * FROM student_knowledge_masteries " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId} AND master_rate < #{threshold}")
    List<StudentKnowledgeMastery> selectWeakKnowledge(@Param("studentId") Long studentId,
                                                      @Param("courseId") Long courseId,
                                                      @Param("threshold") Double threshold);

    /**
     * 查询学生某课程下错误率最高的N个知识点
     */
    @Select("SELECT *, (1 - master_rate / 100) as error_rate " +
            "FROM student_knowledge_masteries " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY master_rate   LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "knowledgeId", column = "knowledge_id"),
            @Result(property = "masterRate", column = "master_rate"),
            @Result(property = "questionCorrectCount", column = "question_correct_count"),
            @Result(property = "questionTotalCount", column = "question_total_count"),
            @Result(property = "errorRate", column = "error_rate"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<StudentKnowledgeMastery> selectTopErrorKnowledge(@Param("studentId") Long studentId, @Param("limit") Integer limit);


    /**
     * 插入知识点掌握情况
     */
    @Insert("INSERT INTO student_knowledge_masteries " +
            "(student_id, course_id, knowledge_id, master_rate, question_correct_count, question_total_count, updated_at) " +
            "VALUES (#{studentId}, #{courseId}, #{knowledgeId}, #{masterRate}, #{questionCorrectCount}, #{questionTotalCount}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(StudentKnowledgeMastery studentKnowledgeMastery);

    /**
     * 更新掌握率（累加答题次数）
     */
    @Update("UPDATE student_knowledge_masteries SET " +
            "master_rate = #{masterRate}, " +
            "question_correct_count = question_correct_count + #{correctIncrement}, " +
            "question_total_count = question_total_count + #{totalIncrement}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long updateMasteryWithIncrement(@Param("id") Long id,
                                    @Param("masterRate") Double masterRate,
                                    @Param("correctIncrement") Integer correctIncrement,
                                    @Param("totalIncrement") Integer totalIncrement);

    /**
     * 更新掌握情况
     */
    @Update("UPDATE student_knowledge_masteries SET " +
            "master_rate = #{masterRate}, " +
            "question_correct_count = #{questionCorrectCount}, " +
            "question_total_count = #{questionTotalCount}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long update(StudentKnowledgeMastery studentKnowledgeMastery);

    /**
     * 根据学生ID和知识点ID更新掌握情况
     */
    @Update("UPDATE student_knowledge_masteries SET " +
            "master_rate = #{masterRate}, " +
            "question_correct_count = #{questionCorrectCount}, " +
            "question_total_count = #{questionTotalCount}, " +
            "course_id = #{courseId}, " +
            "updated_at = NOW() " +
            "WHERE student_id = #{studentId} AND knowledge_id = #{knowledgeId}")
    Long updateByStudentAndKnowledge(StudentKnowledgeMastery studentKnowledgeMastery);

    /**
     * 插入或更新（存在则更新，不存在则插入）
     */
    @Insert("INSERT INTO student_knowledge_masteries " +
            "(student_id, course_id, knowledge_id, master_rate, question_correct_count, question_total_count, updated_at) " +
            "VALUES (#{studentId}, #{courseId}, #{knowledgeId}, #{masterRate}, #{questionCorrectCount}, #{questionTotalCount}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "master_rate = VALUES(master_rate), " +
            "question_correct_count = VALUES(question_correct_count), " +
            "question_total_count = VALUES(question_total_count), " +
            "course_id = VALUES(course_id), " +
            "updated_at = NOW()")
    Long upsert(StudentKnowledgeMastery studentKnowledgeMastery);

    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM student_knowledge_masteries WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据学生ID删除所有掌握情况
     */
    @Delete("DELETE FROM student_knowledge_masteries WHERE student_id = #{studentId}")
    Long deleteByStudentId(Long studentId);

    /**
     * 根据课程ID删除所有掌握情况
     */
    @Delete("DELETE FROM student_knowledge_masteries WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);

    /**
     * 根据学生ID和知识点ID删除
     */
    @Delete("DELETE FROM student_knowledge_masteries WHERE student_id = #{studentId} AND knowledge_id = #{knowledgeId}")
    Long deleteByStudentAndKnowledge(@Param("studentId") Long studentId, @Param("knowledgeId") String knowledgeId);
}
