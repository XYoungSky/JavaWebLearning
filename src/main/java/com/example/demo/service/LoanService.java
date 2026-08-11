package com.example.demo.service;

import com.example.demo.domain.Book;
import com.example.demo.domain.Loan;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.mapper.StudentMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class LoanService {
    private final SqlSessionFactory factory;

    public LoanService(SqlSessionFactory factory) { this.factory = factory; }

    public List<Loan> findAll() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(LoanMapper.class).findAll();
        }
    }

    public void borrow(Long bookId, Long studentId) {
        try (SqlSession session = factory.openSession(false)) {
            try {
                BookMapper books = session.getMapper(BookMapper.class);
                LoanMapper loans = session.getMapper(LoanMapper.class);
                Book book = books.findByIdForUpdate(bookId);
                if (book == null) throw new BusinessException("图书不存在");
                if (session.getMapper(StudentMapper.class).findById(studentId) == null) {
                    throw new BusinessException("学生不存在");
                }
                if (!book.isAvailable() || loans.findActiveByBookId(bookId) != null) {
                    throw new BusinessException("该图书当前不可借阅");
                }
                loans.insert(bookId, studentId);
                books.updateAvailability(bookId, false);
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }
    }

    public void returnBook(Long loanId) {
        try (SqlSession session = factory.openSession(false)) {
            try {
                LoanMapper loans = session.getMapper(LoanMapper.class);
                Loan loan = loans.findActiveByIdForUpdate(loanId);
                if (loan == null) throw new BusinessException("借阅记录不存在或已经归还");
                loans.markReturned(loanId);
                session.getMapper(BookMapper.class).updateAvailability(loan.getBookId(), true);
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }
    }
}
