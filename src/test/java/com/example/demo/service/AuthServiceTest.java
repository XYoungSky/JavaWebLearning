package com.example.demo.service;

import com.example.demo.TestDoubles;
import com.example.demo.domain.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Test
    void returnsSafeUserOnSuccessfulLogin() {
        UserMapper mapper = TestDoubles.proxy(UserMapper.class, (method, args) ->
                new User(1L, "admin", PasswordHasher.hash("secret"), "管理员"));
        TestDoubles.SessionState state = TestDoubles.myBatis(Map.of(UserMapper.class, mapper));

        User result = new AuthService(state.factory).authenticate(" admin ", "secret");

        assertEquals("admin", result.getUsername());
        assertNull(result.getPasswordHash());
        assertTrue(state.closed);
    }

    @Test
    void rejectsMissingOrWrongCredentials() {
        UserMapper mapper = TestDoubles.proxy(UserMapper.class, (method, args) ->
                new User(1L, "admin", PasswordHasher.hash("secret"), "管理员"));
        AuthService service = new AuthService(TestDoubles.myBatis(Map.of(UserMapper.class, mapper)).factory);
        assertThrows(BusinessException.class, () -> service.authenticate("", ""));
        assertThrows(BusinessException.class, () -> service.authenticate("admin", "wrong"));
    }
}
