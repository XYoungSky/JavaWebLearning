package com.example.demo.servlet;

import com.example.demo.config.AppKeys;
import com.example.demo.domain.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.BusinessException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private AuthService authService;

    @Override
    public void init() {
        authService = new AuthService(sqlSessionFactory());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(AppKeys.CURRENT_USER) != null) {
            redirect(request, response, "/books");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        String remembered = readCookie(request, "rememberedUsername");
        model.put("rememberedUsername", remembered);
        model.put("remember", !remembered.isEmpty());
        model.put("next", safeNext(request.getParameter("next")));
        render(request, response, "login", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String next = safeNext(request.getParameter("next"));
        try {
            User user = authService.authenticate(username, request.getParameter("password"));
            request.getSession();
            request.changeSessionId();
            request.getSession().setAttribute(AppKeys.CURRENT_USER, user);
            updateRememberCookie(request, response, username, request.getParameter("remember") != null);
            redirect(request, response, next == null ? "/books" : next);
        } catch (BusinessException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            render(request, response, "login", Map.of(
                    "error", ex.getMessage(),
                    "remember", request.getParameter("remember") != null,
                    "rememberedUsername", username == null ? "" : username,
                    "next", next == null ? "" : next));
        }
    }

    private void updateRememberCookie(HttpServletRequest request, HttpServletResponse response,
                                      String username, boolean remember) {
        Cookie cookie = new Cookie("rememberedUsername", remember ? URLEncoder.encode(username.trim(), StandardCharsets.UTF_8) : "");
        cookie.setHttpOnly(true);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setMaxAge(remember ? 7 * 24 * 60 * 60 : 0);
        response.addCookie(cookie);
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return "";
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                try { return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8); }
                catch (IllegalArgumentException ex) { return ""; }
            }
        }
        return "";
    }

    static String safeNext(String raw) {
        // getParameter() already decoded the query string. Only allow navigable list pages.
        return raw != null && Set.of("/books", "/students", "/loans", "/lab").contains(raw) ? raw : null;
    }
}
