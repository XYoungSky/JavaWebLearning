package com.example.demo.service;

import com.example.demo.TestDoubles;
import com.example.demo.domain.Book;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {
    @Test
    void createsBookAndCommits() {
        AtomicBoolean inserted = new AtomicBoolean();
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) -> switch (method.getName()) {
            case "countByIsbn" -> 0;
            case "insert" -> { inserted.set(true); yield 1; }
            default -> null;
        });
        TestDoubles.SessionState state = state(books, emptyLoans());
        new BookService(state.factory).create("978-1", "Servlet 实战", "作者");
        assertTrue(inserted.get());
        assertEquals(1, state.commits);
        assertEquals(0, state.rollbacks);
    }

    @Test
    void duplicateIsbnRollsBack() {
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) ->
                method.getName().equals("countByIsbn") ? 1 : null);
        TestDoubles.SessionState state = state(books, emptyLoans());
        assertThrows(BusinessException.class,
                () -> new BookService(state.factory).create("978-1", "重复", "作者"));
        assertEquals(1, state.rollbacks);
    }

    @Test
    void cannotDeleteBorrowedBook() {
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) ->
                method.getName().equals("findByIdForUpdate") ? new Book("isbn", "书", "作者") : null);
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) ->
                method.getName().equals("countByBookId") ? 1 : null);
        TestDoubles.SessionState state = state(books, loans);
        assertThrows(BusinessException.class, () -> new BookService(state.factory).delete(9L));
        assertEquals(1, state.rollbacks);
    }

    @Test
    void cannotDeleteBookWithReturnedHistory() {
        Book book = new Book("isbn", "已归还的书", "作者");
        AtomicBoolean deleted = new AtomicBoolean();
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) -> switch (method.getName()) {
            case "findByIdForUpdate" -> book;
            case "delete" -> { deleted.set(true); yield 1; }
            default -> null;
        });
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) ->
                method.getName().equals("countByBookId") ? 1 : 0);
        TestDoubles.SessionState state = state(books, loans);
        BusinessException error = assertThrows(BusinessException.class, () -> new BookService(state.factory).delete(1L));
        assertTrue(error.getMessage().contains("借阅历史"));
        assertFalse(deleted.get());
        assertEquals(1, state.rollbacks);
    }

    @Test
    void deletesBookWithoutHistory() {
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) -> switch (method.getName()) {
            case "findByIdForUpdate" -> new Book("isbn", "书", "作者");
            case "delete" -> 1;
            default -> null;
        });
        TestDoubles.SessionState state = state(books, emptyLoans());
        new BookService(state.factory).delete(1L);
        assertEquals(1, state.commits);
    }

    @Test
    void databaseDuplicateKeyAfterPrecheckIsAFieldError() {
        BookMapper books = TestDoubles.proxy(BookMapper.class, (method, args) -> {
            if (method.getName().equals("insert")) throw new org.apache.ibatis.exceptions.PersistenceException(
                    new java.sql.SQLException("Duplicate entry", "23000", 1062));
            return 0;
        });
        TestDoubles.SessionState state = state(books, emptyLoans());
        BusinessException error = assertThrows(BusinessException.class,
                () -> new BookService(state.factory).create("isbn", "书", "作者"));
        assertEquals("isbn", error.getField());
        assertEquals(1, state.rollbacks);
        assertEquals(0, state.commits);
    }

    private TestDoubles.SessionState state(BookMapper books, LoanMapper loans) {
        return TestDoubles.myBatis(Map.of(BookMapper.class, books, LoanMapper.class, loans));
    }

    private LoanMapper emptyLoans() {
        return TestDoubles.proxy(LoanMapper.class, (method, args) -> null);
    }
}
