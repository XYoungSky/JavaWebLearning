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
                method.getName().equals("findById") ? new Book("isbn", "书", "作者") : null);
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) ->
                method.getName().equals("countActiveByBookId") ? 1 : null);
        TestDoubles.SessionState state = state(books, loans);
        assertThrows(BusinessException.class, () -> new BookService(state.factory).delete(9L));
        assertEquals(1, state.rollbacks);
    }

    private TestDoubles.SessionState state(BookMapper books, LoanMapper loans) {
        return TestDoubles.myBatis(Map.of(BookMapper.class, books, LoanMapper.class, loans));
    }

    private LoanMapper emptyLoans() {
        return TestDoubles.proxy(LoanMapper.class, (method, args) -> null);
    }
}
