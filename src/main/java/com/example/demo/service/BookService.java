package com.example.demo.service;

import com.example.demo.domain.Book;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.util.Validation;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class BookService {
    private final SqlSessionFactory factory;

    public BookService(SqlSessionFactory factory) { this.factory = factory; }

    public List<Book> findAll() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(BookMapper.class).findAll();
        }
    }

    public List<Book> findAvailable() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(BookMapper.class).findAvailable();
        }
    }

    public void create(String isbn, String title, String author) {
        String cleanIsbn = Validation.required(isbn, "ISBN", 32);
        Book book = new Book(cleanIsbn, Validation.required(title, "书名", 120),
                Validation.required(author, "作者", 80));
        try (SqlSession session = factory.openSession(false)) {
            try {
                BookMapper mapper = session.getMapper(BookMapper.class);
                if (mapper.countByIsbn(cleanIsbn) > 0) throw new BusinessException("ISBN 已存在");
                mapper.insert(book);
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }
    }

    public void delete(Long id) {
        try (SqlSession session = factory.openSession(false)) {
            try {
                BookMapper books = session.getMapper(BookMapper.class);
                if (books.findById(id) == null) throw new BusinessException("图书不存在");
                if (session.getMapper(LoanMapper.class).countActiveByBookId(id) > 0) {
                    throw new BusinessException("图书尚未归还，不能删除");
                }
                books.delete(id);
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }
    }
}
