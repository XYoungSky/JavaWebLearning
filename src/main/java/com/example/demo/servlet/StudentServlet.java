package com.example.demo.servlet;

import com.example.demo.service.StudentService;
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
        render(request, response, "students", Map.of("students", service.findAll(), "activePage", "students"));
    }
}
