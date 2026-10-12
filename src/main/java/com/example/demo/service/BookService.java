package com.example.demo.service;

import com.example.demo.domain.Book;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.util.Validation;
import com.example.demo.util.Search;
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

    public List<Book> search(String keyword, String status) {
        Boolean available = "available".equals(status) ? Boolean.TRUE : "borrowed".equals(status) ? Boolean.FALSE : null;
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(BookMapper.class).search(Search.pattern(Search.keyword(keyword)), available);
        }
    }

    public List<Book> findAvailable() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(BookMapper.class).findAvailable();
        }
    }

    public void create(String isbn, String title, String author) {
        String cleanIsbn = Validation.required(isbn, "ISBN", 32, "isbn");
        Book book = new Book(cleanIsbn, Validation.required(title, "书名", 120, "title"),
                Validation.required(author, "作者", 80, "author"));
        try (SqlSession session = factory.openSession(false)) {
            try {
                BookMapper mapper = session.getMapper(BookMapper.class);
                if (mapper.countByIsbn(cleanIsbn) > 0) throw new BusinessException("isbn", "ISBN 已存在");
                mapper.insert(book);
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                if (DatabaseErrors.isDuplicateKey(ex)) throw new BusinessException("isbn", "ISBN 已存在", ex);
                throw ex;
            }
        }
    }

    public void delete(Long id) {
        try (SqlSession session = factory.openSession(false)) {
            try {
                BookMapper books = session.getMapper(BookMapper.class);
                if (books.findByIdForUpdate(id) == null) throw new BusinessException("图书不存在");
                if (session.getMapper(LoanMapper.class).countByBookId(id) > 0) {
                    throw new BusinessException("图书有借阅历史，不能删除；请保留记录以便查询");
                }
                if (books.delete(id) != 1) throw new BusinessException("图书不存在或已被删除");
                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }
    }
}
