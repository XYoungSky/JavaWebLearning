package com.example.demo.servlet;

import com.example.demo.config.AppKeys;
import com.example.demo.util.ThymeleafRenderer;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    protected SqlSessionFactory sqlSessionFactory() {
        return (SqlSessionFactory) getServletContext().getAttribute(AppKeys.SQL_SESSION_FACTORY);
    }

    protected void render(HttpServletRequest request, HttpServletResponse response,
                          String template, Map<String, Object> variables) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> model = new HashMap<>(variables);
        model.put("contextPath", request.getContextPath());
        model.put("applicationName", getServletContext().getInitParameter("applicationName"));
        HttpSession session = request.getSession(false);
        if (session != null) {
            model.put("currentUser", session.getAttribute(AppKeys.CURRENT_USER));
            model.put("flashType", session.getAttribute("flashType"));
            model.put("flashMessage", session.getAttribute("flashMessage"));
            session.removeAttribute("flashType");
            session.removeAttribute("flashMessage");
        }
        ThymeleafRenderer.render(template, model, response.getWriter());
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }
}
