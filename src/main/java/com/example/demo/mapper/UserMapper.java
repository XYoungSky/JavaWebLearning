package com.example.demo.mapper;

import com.example.demo.domain.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
    @Select("SELECT id, username, password_hash, display_name FROM users WHERE username = #{username}")
    User findByUsername(String username);
}
