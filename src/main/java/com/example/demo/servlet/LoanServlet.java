package com.example.demo.servlet;

import com.example.demo.service.BookService;
import com.example.demo.service.BusinessException;
import com.example.demo.service.LoanService;
import com.example.demo.service.StudentService;
import com.example.demo.util.Flash;
import com.example.demo.util.Validation;
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
        Map<String, Object> model = new HashMap<>();
        model.put("loans", loanService.findAll());
        model.put("availableBooks", bookService.findAvailable());
        model.put("students", studentService.findAll());
        model.put("activePage", "loans");
        render(request, response, "loans", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ("/loans/create".equals(request.getServletPath())) {
                Long bookId = Validation.positiveId(request.getParameter("bookId"), "图书编号");
                Long studentId = Validation.positiveId(request.getParameter("studentId"), "学生编号");
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
            Flash.error(request, ex.getMessage());
        }
        redirect(request, response, "/loans");
    }
}
