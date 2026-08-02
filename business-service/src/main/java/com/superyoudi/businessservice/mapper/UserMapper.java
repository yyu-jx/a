package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 登录
 */
@Mapper
public interface UserMapper {

    @Select("SELECT id, username, realname, password, role, avatar_url, created_at, updated_at " +
            "FROM users WHERE username = #{username}")
    Users selectByUsername(String username);
}
