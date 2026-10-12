package com.example.demo.servlet;

import com.example.demo.service.BookService;
import com.example.demo.service.BusinessException;
import com.example.demo.service.LoanService;
import com.example.demo.service.StudentService;
import com.example.demo.util.Flash;
import com.example.demo.util.Validation;
import com.example.demo.util.Search;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet({"/loans", "/loans/create", "/loans/return"})
public class LoanServlet extends BaseServlet {
    private LoanService loanService;
    private BookService bookService;
    private StudentService studentService;

    @Override
    public void init() {
        loanService = new LoanService(sqlSessionFactory());
        bookService = new BookService(sqlSessionFactory());
        studentService = new StudentService(sqlSessionFactory());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/loans".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        renderPage(request, response, null);
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response, BusinessException error) throws IOException {
        String status = Search.status(request.getParameter("status"), "active", "returned");
        Map<String, Object> model = new HashMap<>();
        model.put("status", status);
        model.put("selectedBook", error == null ? "" : java.util.Objects.toString(request.getParameter("bookId"), ""));
        model.put("selectedStudent", error == null ? "" : java.util.Objects.toString(request.getParameter("studentId"), ""));
        model.put("error", error == null ? null : error.getMessage());
        model.put("errorField", error == null ? null : error.getField());
        model.put("loans", loanService.search(status));
        model.put("availableBooks", bookService.findAvailable());
        model.put("students", studentService.findAll());
        model.put("activePage", "loans");
        render(request, response, "loans", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ("/loans/create".equals(request.getServletPath())) {
                Long bookId = Validation.positiveId(request.getParameter("bookId"), "图书编号", "bookId");
                Long studentId = Validation.positiveId(request.getParameter("studentId"), "学生编号", "studentId");
                loanService.borrow(bookId, studentId);
                Flash.success(request, "借阅登记成功");
            } else if ("/loans/return".equals(request.getServletPath())) {
                loanService.returnBook(Validation.positiveId(request.getParameter("id"), "借阅编号"));
                Flash.success(request, "图书归还成功");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (BusinessException ex) {
            if ("/loans/create".equals(request.getServletPath())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                renderPage(request, response, ex);
                return;
            }
            Flash.error(request, ex.getMessage());
        }
        redirect(request, response, "/loans?status=" + Search.status(request.getParameter("status"), "active", "returned"));
    }
}
