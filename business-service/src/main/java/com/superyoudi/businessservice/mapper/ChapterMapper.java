package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Chapter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface ChapterMapper {

    @Select("SELECT * FROM chapters WHERE id = #{id}")
    Chapter selectById(Long id);

    /**
     * 根据课程ID查询所有章节（按排序字段升序）
     */
    @Select("SELECT * FROM chapters WHERE course_id = #{courseId} ORDER BY sort_order ASC")
    List<Chapter> selectByCourseId(Long courseId);

    /**
     * 根据课程ID和排序查询章节
     */
    @Select("SELECT * FROM chapters WHERE course_id = #{courseId} AND sort_order = #{sortOrder}")
    Chapter selectByCourseIdAndSortOrder(@Param("courseId") Long courseId, @Param("sortOrder") Integer sortOrder);

    /**
     * 根据课程ID统计章节数量
     */
    @Select("SELECT COUNT(*) FROM chapters WHERE course_id = #{courseId}")
    Long countByCourseId(Long courseId);

    /**
     * 获取课程中最大的排序值
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM chapters WHERE course_id = #{courseId}")
    Integer selectMaxSortOrder(Long courseId);

    /**
     * 插入章节
     */
    @Insert("INSERT INTO chapters (course_id, name, summary, sort_order, created_at, updated_at) " +
            "VALUES (#{courseId}, #{name}, #{summary}, #{sortOrder}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(Chapter chapter);


    /**
     * 更新章节
     */
    @Update("UPDATE chapters SET " +
            "name = #{name}, " +
            "summary = #{summary}, " +
            "sort_order = #{sortOrder}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long update(Chapter chapter);

    /**
     * 更新章节排序
     */
    @Update("UPDATE chapters SET sort_order = #{sortOrder}, updated_at = NOW() WHERE id = #{id}")
    Long updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);


    /**
     * 根据ID删除章节
     */
    @Delete("DELETE FROM chapters WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据课程ID删除所有章节
     */
    @Delete("DELETE FROM chapters WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);
}