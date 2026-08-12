package com.superyoudi.coursewareservice.mapper;

import com.superyoudi.coursewareservice.pojo.entity.CoursewareFileRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface CoursewareFileRelMapper {

    /**
     * 插入关联记录
     */
    @Insert("INSERT INTO courseware_file_rel (courseware_record_id, file_id) " +
            "VALUES (#{coursewareRecordId}, #{fileId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(CoursewareFileRel coursewareFileRel);

    /**
     * 根据ID删除关联记录
     */
    @Delete("DELETE FROM courseware_file_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据ID查询关联记录
     */
    @Select("SELECT * FROM courseware_file_rel WHERE id = #{id}")
    CoursewareFileRel findById(Long id);

    /**
     * 根据课件记录ID查询所有关联文件
     */
    @Select("SELECT * FROM courseware_file_rel WHERE courseware_record_id = #{coursewareRecordId}")
    List<CoursewareFileRel> findByCoursewareRecordId(Long coursewareRecordId);

    /**
     * 根据文件ID查询所有关联的课件记录
     */
    @Select("SELECT * FROM courseware_file_rel WHERE file_id = #{fileId}")
    List<CoursewareFileRel> findByFileId(Long fileId);

    /**
     * 根据课件记录ID删除所有关联
     */
    @Delete("DELETE FROM courseware_file_rel WHERE courseware_record_id = #{coursewareRecordId}")
    Long deleteByCoursewareRecordId(Long coursewareRecordId);

    /**
     * 根据文件ID删除所有关联
     */
    @Delete("DELETE FROM courseware_file_rel WHERE file_id = #{fileId}")
    Long deleteByFileId(Long fileId);

    /**
     * 检查关联是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM courseware_file_rel " +
            "WHERE courseware_record_id = #{coursewareRecordId} AND file_id = #{fileId}")
    boolean existsByCoursewareAndFile(@Param("coursewareRecordId") Long coursewareRecordId,
                                      @Param("fileId") Long fileId);

    /**
     * 根据课件记录ID和文件ID删除关联
     */
    @Delete("DELETE FROM courseware_file_rel " +
            "WHERE courseware_record_id = #{coursewareRecordId} AND file_id = #{fileId}")
    Long deleteByCoursewareAndFile(@Param("coursewareRecordId") Long coursewareRecordId,
                                   @Param("fileId") Long fileId);

    /**
     * 统计课件关联的文件数量
     */
    @Select("SELECT COUNT(*) FROM courseware_file_rel WHERE courseware_record_id = #{coursewareRecordId}")
    Long countByCoursewareRecordId(Long coursewareRecordId);

    /**
     * 统计文件被关联的课件数量
     */
    @Select("SELECT COUNT(*) FROM courseware_file_rel WHERE file_id = #{fileId}")
    Long countByFileId(Long fileId);

}
