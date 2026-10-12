package com.example.demo.util;

import com.example.demo.domain.Book;
import com.example.demo.domain.Loan;
import com.example.demo.domain.Student;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThymeleafRendererTest {
    @Test
    void rendersChineseContentLoopAndCondition() {
        Book book = new Book("978-test", "Servlet 实战", "测试作者");
        StringWriter output = new StringWriter();

        ThymeleafRenderer.render("books", Map.of(
                "contextPath", "/library",
                "applicationName", "测试图书系统",
                "activePage", "books",
                "books", List.of(book)
        ), output);

        String html = output.toString();
        assertTrue(html.contains("Servlet 实战"));
        assertTrue(html.contains("测试作者"));
        assertTrue(html.contains("可借"));
        assertTrue(html.contains("/library/assets/app.css"));
    }

    @Test
    void rendersAllRemainingTemplates() {
        Map<String, Object> base = Map.of(
                "contextPath", "/library",
                "applicationName", "测试图书系统",
                "activePage", "lab"
        );
        assertContains("login", Map.of(
                "contextPath", "/library", "rememberedUsername", "admin", "next", ""), "管理员登录");
        assertContains("students", merge(base, Map.of(
                "students", List.of(new Student("S01", "测试学生", "1 班")))), "测试学生");

        Loan loan = new Loan();
        loan.setId(1L); loan.setBookId(2L); loan.setStudentId(3L);
        loan.setBorrowedAt(java.time.LocalDateTime.of(2026, 10, 12, 12, 30));
        loan.setBookTitle("借阅测试书"); loan.setIsbn("ISBN-1");
        loan.setStudentName("借阅学生"); loan.setStudentNo("S02");
        assertContains("loans", merge(base, Map.of(
                "loans", List.of(loan), "availableBooks", List.of(), "students", List.of())), "借阅测试书");

        assertContains("lab", merge(base, Map.ofEntries(
                Map.entry("method", "GET"), Map.entry("protocol", "HTTP/1.1"),
                Map.entry("requestUrl", "http://localhost/library/lab"), Map.entry("remoteAddress", "127.0.0.1"),
                Map.entry("headers", Map.of("accept", "text/html")), Map.entry("parameters", Map.of()),
                Map.entry("cookies", List.of()), Map.entry("session", new SessionView()),
                Map.entry("activeSessions", 1), Map.entry("requestCount", 2L), Map.entry("serviceCalls", 1L),
                Map.entry("initializedAt", "now"), Map.entry("servletInitParam", "lesson"),
                Map.entry("contextInitParam", "app"), Map.entry("forwardedMessage", "forward ok"),
                Map.entry("redirectArrived", false), Map.entry("classLoaders", List.of("WebappClassLoader"))
        )), "forward ok");
    }

    @Test
    void rendersRetainedValuesAndAccessibleFieldError() {
        StringWriter output = new StringWriter();
        ThymeleafRenderer.render("books", Map.of(
                "contextPath", "/library", "activePage", "books", "books", List.of(),
                "q", "Java", "status", "available", "error", "ISBN 已存在", "errorField", "isbn",
                "form", Map.of("isbn", "duplicate", "title", "<测试书>", "author", "作者")
        ), output);
        String html = output.toString();
        assertTrue(html.contains("aria-describedby=\"error-isbn\""));
        assertTrue(html.contains("value=\"&lt;测试书&gt;\""));
        assertTrue(html.contains("没有匹配的图书"));
        assertTrue(html.contains("</label>"));
        assertTrue(html.chars().noneMatch(c -> c < 32 && c != 9 && c != 10 && c != 13));
    }

    private void assertContains(String template, Map<String, Object> model, String expected) {
        StringWriter output = new StringWriter();
        ThymeleafRenderer.render(template, model, output);
        assertTrue(output.toString().contains(expected), template + " should contain " + expected);
    }

    private Map<String, Object> merge(Map<String, Object> first, Map<String, Object> second) {
        java.util.HashMap<String, Object> result = new java.util.HashMap<>(first);
        result.putAll(second);
        return result;
    }

    public static class SessionView {
        public String getId() { return "session-id"; }
        public boolean isNew() { return true; }
    }
}
