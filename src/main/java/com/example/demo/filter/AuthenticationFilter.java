package com.example.demo.filter;

import com.example.demo.config.AppKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@WebFilter(filterName = "02AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter extends HttpFilter {
    private static final Set<String> PUBLIC_EXACT = Set.of("/login", "/api/time", "/favicon.ico");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String path = pathWithinApplication(request);
        if (isPublic(path) || hasAuthenticatedUser(request)) {
            chain.doFilter(request, response);
            return;
        }
        String next = URLEncoder.encode(path, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?next=" + next);
    }

    static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return uri.substring(contextPath.length());
    }

    static boolean isPublic(String path) {
        return PUBLIC_EXACT.contains(path) || path.startsWith("/assets/") || path.startsWith("/lab");
    }

    private boolean hasAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(AppKeys.CURRENT_USER) != null;
    }
}
