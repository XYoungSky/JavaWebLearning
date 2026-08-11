package com.example.demo.servlet;

import com.example.demo.config.AppKeys;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@WebServlet(value = {"/lab", "/lab/*"}, loadOnStartup = 1, initParams = {
        @WebInitParam(name = "lesson", value = "Servlet 生命周期与 HTTP 诊断")
})
public class LabServlet extends BaseServlet {
    private static final Logger LOG = Logger.getLogger(LabServlet.class.getName());
    private final AtomicLong serviceCalls = new AtomicLong();
    private String initializedAt;

    public LabServlet() {
        LOG.info("LabServlet 构造方法执行");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initializedAt = java.time.ZonedDateTime.now().toString();
        LOG.info(() -> "LabServlet init 执行，初始化参数：" + getInitParameter("lesson"));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        serviceCalls.incrementAndGet();
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/navigation".equals(path)) {
            navigation(request, response);
            return;
        }
        if ("/navigation-target".equals(path)) {
            renderLab(request, response);
            return;
        }
        if (path != null && !"/".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        renderLab(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if ("/cookie".equals(path)) {
            String action = request.getParameter("action");
            Cookie cookie = new Cookie("labCookie", "create".equals(action) ? "Servlet-Cookie-Demo" : "");
            cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
            cookie.setHttpOnly(true);
            cookie.setMaxAge("create".equals(action) ? 60 : 0);
            response.addCookie(cookie);
            redirect(request, response, "/lab");
            return;
        }
        if ("/session/invalidate".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            redirect(request, response, "/lab");
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void destroy() {
        LOG.info(() -> "LabServlet destroy 执行，累计 service 次数：" + serviceCalls.get());
    }

    private void navigation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("forward".equals(request.getParameter("mode"))) {
            request.setAttribute("forwardedMessage", "该属性由转发前的 Servlet 写入，目标处理器仍可读取。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/lab/navigation-target");
            dispatcher.forward(request, response);
        } else {
            redirect(request, response, "/lab?arrived=redirect");
        }
    }

    private void renderLab(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("activePage", "lab");
        model.put("method", request.getMethod());
        model.put("protocol", request.getProtocol());
        model.put("requestUrl", request.getRequestURL().toString());
        model.put("remoteAddress", request.getRemoteAddr());
        model.put("headers", headers(request));
        model.put("parameters", request.getParameterMap());
        model.put("cookies", request.getCookies() == null ? List.of() : List.of(request.getCookies()));
        model.put("session", request.getSession());
        model.put("activeSessions", ((AtomicInteger) getServletContext().getAttribute(AppKeys.ACTIVE_SESSIONS)).get());
        model.put("requestCount", ((AtomicLong) getServletContext().getAttribute(AppKeys.REQUEST_COUNT)).get());
        model.put("serviceCalls", serviceCalls.get());
        model.put("initializedAt", initializedAt);
        model.put("servletInitParam", getInitParameter("lesson"));
        model.put("contextInitParam", getServletContext().getInitParameter("applicationName"));
        model.put("forwardedMessage", request.getAttribute("forwardedMessage"));
        model.put("redirectArrived", "redirect".equals(request.getParameter("arrived")));
        model.put("classLoaders", classLoaders());
        render(request, response, "lab", model);
    }

    private Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        List<String> names = Collections.list(request.getHeaderNames());
        names.forEach(name -> values.put(name, request.getHeader(name)));
        return values;
    }

    private List<String> classLoaders() {
        List<String> values = new ArrayList<>();
        ClassLoader loader = getClass().getClassLoader();
        while (loader != null) {
            values.add(loader.toString());
            loader = loader.getParent();
        }
        values.add("Bootstrap ClassLoader (JVM 内置，Java 中表示为 null)");
        return values;
    }
}
