package com.example.demo.service;

import com.example.demo.TestDoubles;
import com.example.demo.domain.Book;
import com.example.demo.domain.Loan;
import com.example.demo.domain.Student;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.mapper.StudentMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {
    @Test
    void borrowingUpdatesBothRecordsInOneTransaction() {
        AtomicInteger changes = new AtomicInteger();
        Book book = new Book("isbn", "书", "作者");
        BookMapper books = bookMapper(book, changes, false);
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) -> {
            if (method.getName().equals("insert")) changes.incrementAndGet();
            return null;
        });
        TestDoubles.SessionState state = state(books, loans);
        new LoanService(state.factory).borrow(1L, 2L);
        assertEquals(2, changes.get());
        assertEquals(1, state.commits);
    }

    @Test
    void unavailableBookCannotBeBorrowed() {
        Book book = new Book("isbn", "书", "作者");
        book.setAvailable(false);
        TestDoubles.SessionState state = state(bookMapper(book, new AtomicInteger(), false), emptyLoans());
        assertThrows(BusinessException.class, () -> new LoanService(state.factory).borrow(1L, 2L));
        assertEquals(1, state.rollbacks);
    }

    @Test
    void returningBookUpdatesLoanAndAvailability() {
        AtomicInteger changes = new AtomicInteger();
        Loan loan = new Loan(); loan.setId(8L); loan.setBookId(3L);
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) -> switch (method.getName()) {
            case "findActiveByIdForUpdate" -> loan;
            case "markReturned" -> { changes.incrementAndGet(); yield 1; }
            default -> null;
        });
        TestDoubles.SessionState state = state(bookMapper(null, changes, false), loans);
        new LoanService(state.factory).returnBook(8L);
        assertEquals(2, changes.get());
        assertEquals(1, state.commits);
    }

    @Test
    void transactionFailureRollsBack() {
        Book book = new Book("isbn", "书", "作者");
        LoanMapper loans = TestDoubles.proxy(LoanMapper.class, (method, args) -> {
            if (method.getName().equals("insert")) throw new IllegalStateException("database failure");
            return null;
        });
        TestDoubles.SessionState state = state(bookMapper(book, new AtomicInteger(), false), loans);
        assertThrows(IllegalStateException.class, () -> new LoanService(state.factory).borrow(1L, 2L));
        assertEquals(1, state.rollbacks);
    }

    private TestDoubles.SessionState state(BookMapper books, LoanMapper loans) {
        StudentMapper students = TestDoubles.proxy(StudentMapper.class, (method, args) ->
                method.getName().equals("findById") ? new Student("S1", "学生", "1 班") : null);
        return TestDoubles.myBatis(Map.of(BookMapper.class, books, LoanMapper.class, loans, StudentMapper.class, students));
    }

    private BookMapper bookMapper(Book book, AtomicInteger changes, boolean fail) {
        return TestDoubles.proxy(BookMapper.class, (method, args) -> switch (method.getName()) {
            case "findByIdForUpdate" -> book;
            case "updateAvailability" -> { changes.incrementAndGet(); yield 1; }
            default -> null;
        });
    }

    private LoanMapper emptyLoans() {
        return TestDoubles.proxy(LoanMapper.class, (method, args) -> null);
    }
}
