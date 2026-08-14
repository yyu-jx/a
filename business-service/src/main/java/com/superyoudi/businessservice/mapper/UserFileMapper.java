package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.UserFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface UserFileMapper {

    /**
     * 插入文件记录
     */
    @Insert("INSERT INTO user_files (user_id, file_name, file_desc, file_url, size, type, source) " +
            "VALUES (#{userId}, #{fileName}, #{fileDesc}, #{fileUrl}, #{size}, #{type}, #{source})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(UserFile userFile);

    /**
     * 根据ID删除文件记录
     */
    @Delete("DELETE FROM user_files WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据ID更新文件记录
     */
    @Update("""
            UPDATE user_files SET \
            user_id = #{userId}, \
            file_name = #{fileName}, \
            file_desc = #{fileDesc}, \
            file_url = #{fileUrl}, \
            size = #{size}, \
            type = #{type}, \
            source = #{source} \
            WHERE id = #{id}""")
    Long update(UserFile userFile);

    /**
     * 根据ID查询文件记录
     */
    @Select("SELECT * FROM user_files WHERE id = #{id}")
    UserFile findById(Long id);

    /**
     * 查询所有文件记录
     */
    @Select("SELECT * FROM user_files ORDER BY created_at DESC")
    List<UserFile> findAll();

    /**
     * 根据用户ID查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserFile> findByUserId(Long userId);

    /**
     * 根据文件类型查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE type = #{type} ORDER BY created_at DESC")
    List<UserFile> findByType(String type);

    /**
     * 根据文件来源查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE source = #{source} ORDER BY created_at DESC")
    List<UserFile> findBySource(String source);

    /**
     * 根据用户ID和文件类型查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} AND type = #{type} " +
            "ORDER BY created_at DESC")
    List<UserFile> findByUserIdAndType(@Param("userId") Long userId,
                                       @Param("type") String type);

    /**
     * 根据用户ID和文件来源查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} AND source = #{source} " +
            "ORDER BY created_at DESC")
    List<UserFile> findByUserIdAndSource(@Param("userId") Long userId,
                                         @Param("source") String source);

    /**
     * 根据用户ID、文件类型和来源查询文件列表
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} AND type = #{type} AND source = #{source} " +
            "ORDER BY created_at DESC")
    List<UserFile> findByUserIdAndTypeAndSource(@Param("userId") Long userId,
                                                @Param("type") String type,
                                                @Param("source") String source);


    /**
     * 根据用户ID删除所有文件
     */
    @Delete("DELETE FROM user_files WHERE user_id = #{userId}")
    Long deleteByUserId(Long userId);

    /**
     * 根据文件类型删除文件
     */
    @Delete("DELETE FROM user_files WHERE type = #{type}")
    Long deleteByType(String type);

    /**
     * 统计用户文件总数
     */
    @Select("SELECT COUNT(*) FROM user_files WHERE user_id = #{userId}")
    Long countByUserId(Long userId);

    /**
     * 统计用户各类型文件数量
     */
    @Select("SELECT type, COUNT(*) as count FROM user_files " +
            "WHERE user_id = #{userId} GROUP BY type")
    List<java.util.Map<String, Object>> countByTypeForUser(Long userId);

    /**
     * 统计用户各来源文件数量
     */
    @Select("SELECT source, COUNT(*) as count FROM user_files " +
            "WHERE user_id = #{userId} GROUP BY source")
    List<java.util.Map<String, Object>> countBySourceForUser(Long userId);

    /**
     * 统计文件总大小（字节）
     */
    @Select("SELECT SUM(size) FROM user_files WHERE user_id = #{userId}")
    Long sumSizeByUserId(Long userId);

    /**
     * 分页查询用户文件
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<UserFile> findByUserIdWithPage(@Param("userId") Long userId,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    /**
     * 根据文件名模糊搜索
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} " +
            "AND file_name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC")
    List<UserFile> searchByFileName(@Param("userId") Long userId,
                                    @Param("keyword") String keyword);

    /**
     * 查询最近上传的文件
     */
    @Select("SELECT * FROM user_files WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<UserFile> findRecentFiles(@Param("userId") Long userId,
                                   @Param("limit") Integer limit);

    /**
     * 根据文件URL查询文件
     */
    @Select("SELECT * FROM user_files WHERE file_url = #{fileUrl}")
    UserFile findByFileUrl(String fileUrl);

    /**
     * 检查文件是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user_files WHERE id = #{id}")
    boolean existsById(Long id);

    /**
     * 检查用户是否拥有该文件
     */
    @Select("SELECT COUNT(*) > 0 FROM user_files WHERE id = #{id} AND user_id = #{userId}")
    boolean existsByIdAndUserId(@Param("id") Long id,
                                @Param("userId") Long userId);

    /**
     * 更新文件描述
     */
    @Update("UPDATE user_files SET file_desc = #{fileDesc} WHERE id = #{id}")
    Long updateFileDesc(@Param("id") Long id,
                        @Param("fileDesc") String fileDesc);

    /**
     * 更新文件名
     */
    @Update("UPDATE user_files SET file_name = #{fileName} WHERE id = #{id}")
    Long updateFileName(@Param("id") Long id,
                        @Param("fileName") String fileName);

}
