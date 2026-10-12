package com.example.demo.servlet;

import com.example.demo.TestDoubles;
import com.example.demo.config.AppKeys;
import com.example.demo.domain.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.PasswordHasher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class LoginServletTest {
    @Test
    void acceptsOnlyKnownPagesWithoutDecodingAgain() {
        assertEquals("/books", LoginServlet.safeNext("/books"));
        for (String invalid : new String[]{"%", "/%", "//evil.example", "/../login", "/books/delete", "/%2fhost", "/\\evil", "/books?next=x"}) {
            assertNull(LoginServlet.safeNext(invalid));
        }
        assertNull(LoginServlet.safeNext(null));
    }

    @Test
    void successfulLoginRotatesSessionAndRedirects() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setPasswordHash(PasswordHasher.hash("test-password"));
        UserMapper mapper = TestDoubles.proxy(UserMapper.class, (method, args) -> user);
        var state = TestDoubles.myBatis(Map.of(UserMapper.class, mapper));
        ServletContext context = TestDoubles.proxy(ServletContext.class, (method, args) -> state.factory);
        ServletConfig config = TestDoubles.proxy(ServletConfig.class, (method, args) -> method.getName().equals("getServletContext") ? context : null);
        AtomicBoolean rotated = new AtomicBoolean();
        AtomicReference<Object> authenticated = new AtomicReference<>();
        HttpSession session = TestDoubles.proxy(HttpSession.class, (method, args) -> {
            if (method.getName().equals("setAttribute") && args[0].equals(AppKeys.CURRENT_USER)) authenticated.set(args[1]);
            return null;
        });
        Map<String, String> parameters = Map.of("username", "admin", "password", "test-password", "next", "/students");
        HttpServletRequest request = TestDoubles.proxy(HttpServletRequest.class, (method, args) -> switch (method.getName()) {
            case "getParameter" -> parameters.get(args[0]);
            case "getSession" -> session;
            case "getContextPath" -> "/library";
            case "changeSessionId" -> { rotated.set(true); yield "new-id"; }
            default -> null;
        });
        AtomicReference<String> location = new AtomicReference<>();
        HttpServletResponse response = TestDoubles.proxy(HttpServletResponse.class, (method, args) -> {
            if (method.getName().equals("sendRedirect")) location.set((String) args[0]);
            return null;
        });
        LoginServlet servlet = new LoginServlet();
        servlet.init(config);
        servlet.doPost(request, response);
        assertTrue(rotated.get());
        assertNotNull(authenticated.get());
        assertEquals("/library/students", location.get());
    }
}
