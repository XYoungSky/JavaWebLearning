package com.example.demo.servlet;

import com.example.demo.service.BookService;
import com.example.demo.service.BusinessException;
import com.example.demo.util.Flash;
import com.example.demo.util.Validation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet({"/books", "/books/create", "/books/delete"})
public class BookServlet extends BaseServlet {
    private BookService service;

    @Override
    public void init() { service = new BookService(sqlSessionFactory()); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/books".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        render(request, response, "books", Map.of("books", service.findAll(), "activePage", "books"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ("/books/create".equals(request.getServletPath())) {
                service.create(request.getParameter("isbn"), request.getParameter("title"), request.getParameter("author"));
                Flash.success(request, "图书添加成功");
            } else if ("/books/delete".equals(request.getServletPath())) {
                service.delete(Validation.positiveId(request.getParameter("id"), "图书编号"));
                Flash.success(request, "图书删除成功");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (BusinessException ex) {
            Flash.error(request, ex.getMessage());
        }
        redirect(request, response, "/books");
    }
}
