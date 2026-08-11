package com.superyoudi.businessservice.mapper;

/**
 * @author Sijayyx
 */

import com.superyoudi.businessservice.pojo.entity.Clazz;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClazzMapper {

    /**
     * 插入一条记录
     */
    @Insert("INSERT INTO classes (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(Clazz clazz);

    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM classes WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 更新记录
     */
    @Update("UPDATE classes SET name = #{name} WHERE id = #{id}")
    Long update(Clazz clazz);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM classes WHERE id = #{id}")
    Clazz selectById(Long id);

    /**
     * 根据班级名称查询
     */
    @Select("SELECT * FROM classes WHERE name = #{name}")
    Clazz selectByName(String name);

    /**
     * 模糊查询班级名称
     */
    @Select("SELECT * FROM classes WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Clazz> selectByNameLike(String name);


    /**
     * 查询所有记录
     */
    @Select("SELECT * FROM classes ORDER BY id DESC")
    List<Clazz> selectAll();


    /**
     * 统计总记录数
     */
    @Select("SELECT COUNT(*) FROM classes")
    long count();


}