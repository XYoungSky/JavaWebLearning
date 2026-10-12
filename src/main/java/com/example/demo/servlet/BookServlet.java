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
import java.util.HashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.example.demo.util.Search;

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
        renderPage(request, response, null);
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
            if ("/books/create".equals(request.getServletPath())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                renderPage(request, response, ex);
                return;
            }
            Flash.error(request, ex.getMessage());
        }
        redirect(request, response, "/books?q=" + URLEncoder.encode(Search.keyword(request.getParameter("q")), StandardCharsets.UTF_8)
                + "&status=" + Search.status(request.getParameter("status"), "available", "borrowed"));
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response, BusinessException error) throws IOException {
        String keyword = Search.keyword(request.getParameter("q"));
        String status = Search.status(request.getParameter("status"), "available", "borrowed");
        Map<String, Object> model = new HashMap<>();
        model.put("books", service.search(keyword, status));
        model.put("activePage", "books");
        model.put("q", keyword);
        model.put("status", status);
        Map<String, String> form = new HashMap<>();
        for (String field : new String[]{"isbn", "title", "author"}) {
            form.put(field, error == null ? "" : java.util.Objects.toString(request.getParameter(field), ""));
        }
        model.put("form", form);
        model.put("error", error == null ? null : error.getMessage());
        model.put("errorField", error == null ? null : error.getField());
        render(request, response, "books", model);
    }
}
