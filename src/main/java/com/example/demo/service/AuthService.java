package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.PasswordHasher;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class AuthService {
    private final SqlSessionFactory factory;

    public AuthService(SqlSessionFactory factory) {
        this.factory = factory;
    }

    public User authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("用户名和密码不能为空");
        }
        try (SqlSession session = factory.openSession()) {
            User user = session.getMapper(UserMapper.class).findByUsername(username.trim());
            if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
                throw new BusinessException("用户名或密码错误");
            }
            return user.safeCopy();
        }
    }
}
