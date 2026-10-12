package com.example.demo.servlet;

import com.example.demo.service.StudentService;
import com.example.demo.util.Search;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/students")
public class StudentServlet extends BaseServlet {
    private StudentService service;

    @Override
    public void init() { service = new StudentService(sqlSessionFactory()); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = Search.keyword(request.getParameter("q"));
        render(request, response, "students", Map.of("students", service.search(keyword), "activePage", "students", "q", keyword));
    }
}
