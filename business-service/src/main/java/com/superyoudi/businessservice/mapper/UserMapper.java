package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 登录
 */
@Mapper
public interface UserMapper {

    @Select("SELECT role FROM users WHERE id = #{userId}")
    String selectUserRoleById(Long userId);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    /**
     * 根据用户名/学号查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);

    /**
     * 根据角色查询用户列表
     */
    @Select("SELECT * FROM users WHERE role = #{role}")
    List<User> selectByRole(String role);

    /**
     * 根据真实姓名模糊查询
     */
    @Select("SELECT * FROM users WHERE realname LIKE CONCAT('%', #{realname}, '%')")
    List<User> selectByRealName(String realname);

    /**
     * 查询所有用户
     */
    @Select("SELECT * FROM users")
    List<User> selectAll();

    /**
     * 插入用户
     */
    @Insert("INSERT INTO users (username, realname, password, role, avatar_url, created_at, updated_at) " +
            "VALUES (#{username}, #{realname}, #{password}, #{role}, #{avatarUrl}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(User user);


    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET " +
            "realname = #{realname}, " +
            "role = #{role}, " +
            "avatar_url = #{avatarUrl}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long update(User user);

    /**
     * 更新密码
     */
    @Update("UPDATE users SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    Long updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新头像
     */
    @Update("UPDATE users SET avatar_url = #{avatarUrl}, updated_at = NOW() WHERE id = #{id}")
    Long updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

    /**
     * 更新用户名
     */
    @Update("UPDATE users SET username = #{username}, updated_at = NOW() WHERE id = #{id}")
    Long updateUsername(@Param("id") Long id, @Param("username") String username);

    /**
     * 根据ID删除用户
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据用户名删除用户
     */
    @Delete("DELETE FROM users WHERE username = #{username}")
    Long deleteByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    Long countByUsername(String username);
}
