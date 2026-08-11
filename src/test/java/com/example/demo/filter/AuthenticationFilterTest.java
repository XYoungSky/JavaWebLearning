package com.example.demo.filter;

import com.example.demo.TestDoubles;
import com.example.demo.config.AppKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTest {
    @Test
    void allowsPublicLabWithoutSession() throws Exception {
        AtomicBoolean chained = new AtomicBoolean();
        invoke("/app/lab", null, chained, new AtomicReference<>());
        assertTrue(chained.get());
    }

    @Test
    void redirectsAnonymousBusinessRequest() throws Exception {
        AtomicReference<String> redirect = new AtomicReference<>();
        invoke("/app/books", null, new AtomicBoolean(), redirect);
        assertEquals("/app/login?next=%2Fbooks", redirect.get());
    }

    @Test
    void allowsAuthenticatedRequest() throws Exception {
        HttpSession session = TestDoubles.proxy(HttpSession.class, (method, args) ->
                method.getName().equals("getAttribute") && AppKeys.CURRENT_USER.equals(args[0]) ? new Object() : null);
        AtomicBoolean chained = new AtomicBoolean();
        invoke("/app/loans", session, chained, new AtomicReference<>());
        assertTrue(chained.get());
    }

    private void invoke(String uri, HttpSession session, AtomicBoolean chained,
                        AtomicReference<String> redirect) throws Exception {
        HttpServletRequest request = TestDoubles.proxy(HttpServletRequest.class, (method, args) -> switch (method.getName()) {
            case "getRequestURI" -> uri;
            case "getContextPath" -> "/app";
            case "getSession" -> session;
            default -> null;
        });
        HttpServletResponse response = TestDoubles.proxy(HttpServletResponse.class, (method, args) -> {
            if (method.getName().equals("sendRedirect")) redirect.set((String) args[0]);
            return null;
        });
        FilterChain chain = TestDoubles.proxy(FilterChain.class, (method, args) -> {
            if (method.getName().equals("doFilter")) chained.set(true);
            return null;
        });
        new AuthenticationFilter().doFilter(request, response, chain);
    }
}
