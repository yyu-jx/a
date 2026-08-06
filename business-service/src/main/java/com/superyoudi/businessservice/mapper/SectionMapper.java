package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Section;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SectionMapper {

    @Select("SELECT * FROM sections WHERE id = #{id}")
    Section selectById(Long id);

    /**
     * 根据章ID查询所有小节（按排序字段升序）
     */
    @Select("SELECT * FROM sections WHERE chapter_id = #{chapterId} ORDER BY sort_order ASC")
    List<Section> selectByChapterId(Long chapterId);

    /**
     * 根据章ID和排序查询小节
     */
    @Select("SELECT * FROM sections WHERE chapter_id = #{chapterId} AND sort_order = #{sortOrder}")
    Section selectByChapterIdAndSortOrder(@Param("chapterId") Long chapterId, @Param("sortOrder") Integer sortOrder);

    /**
     * 根据章ID统计小节数量
     */
    @Select("SELECT COUNT(*) FROM sections WHERE chapter_id = #{chapterId}")
    Long countByChapterId(Long chapterId);

    /**
     * 获取章中最大的排序值
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM sections WHERE chapter_id = #{chapterId}")
    Integer selectMaxSortOrder(Long chapterId);

    /**
     * 插入小节
     */
    @Insert("INSERT INTO sections (chapter_id, name, summary, sort_order, created_at, updated_at) " +
            "VALUES (#{chapterId}, #{name}, #{summary}, #{sortOrder}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(Section section);

    /**
     * 更新小节
     */
    @Update("UPDATE sections SET " +
            "name = #{name}, " +
            "summary = #{summary}, " +
            "sort_order = #{sortOrder}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long update(Section section);

    /**
     * 更新小节排序
     */
    @Update("UPDATE sections SET sort_order = #{sortOrder}, updated_at = NOW() WHERE id = #{id}")
    Long updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);


    /**
     * 根据ID删除小节
     */
    @Delete("DELETE FROM sections WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据章ID删除所有小节
     */
    @Delete("DELETE FROM sections WHERE chapter_id = #{chapterId}")
    Long deleteByChapterId(Long chapterId);
}