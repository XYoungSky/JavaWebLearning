package com.example.demo.integration;

import com.example.demo.config.DatabaseConfig;
import com.example.demo.mapper.BookMapper;
import com.example.demo.service.BookService;
import com.example.demo.service.BusinessException;
import com.example.demo.service.LoanService;
import com.example.demo.service.StudentService;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in tests create and remove only their own randomly named schema, never the configured study schema. */
@EnabledIfEnvironmentVariable(named = "JAVAWEB_RUN_DB_TESTS", matches = "true")
class DatabaseIntegrationTest {
    private static Connection admin;
    private static String database;
    private static boolean created;
    private static Properties settings;
    private static SqlSessionFactory factory;
    private long bookId;
    private long studentId;

    @BeforeAll
    static void initialize() throws Exception {
        settings = DatabaseConfig.load();
        admin = DriverManager.getConnection(settings.getProperty("db.url"), settings.getProperty("db.username"), settings.getProperty("db.password"));
        database = "javaweb_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Statement statement = admin.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + database + " CHARACTER SET utf8mb4");
            created = true;
        }
        String url = settings.getProperty("db.url").replaceFirst("(jdbc:mysql://[^/]+/)[^?]*", "$1" + database);
        if (!url.contains("/" + database)) throw new IllegalStateException("Integration tests require a single-host MySQL JDBC URL");
        settings.setProperty("db.url", url);
        factory = DatabaseConfig.createFactory(settings);
        String schema = Files.readString(Path.of("schema.sql")).replaceAll("(?m)^--.*$", "");
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) if (!sql.isBlank()) statement.execute(sql);
        }
        System.out.println("Database integration tests use isolated schema: " + database);
    }

    @BeforeEach
    void seed() throws Exception {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM loans");
            statement.executeUpdate("DELETE FROM books");
            statement.executeUpdate("DELETE FROM students");
            statement.executeUpdate("INSERT INTO books(isbn,title,author) VALUES ('test-isbn','事务测试书','测试作者')", Statement.RETURN_GENERATED_KEYS);
            try (ResultSet keys = statement.getGeneratedKeys()) { keys.next(); bookId = keys.getLong(1); }
            statement.executeUpdate("INSERT INTO students(student_no,name,class_name) VALUES ('T01','测试学生','测试班')", Statement.RETURN_GENERATED_KEYS);
            try (ResultSet keys = statement.getGeneratedKeys()) { keys.next(); studentId = keys.getLong(1); }
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        if (factory != null) ((PooledDataSource) factory.getConfiguration().getEnvironment().getDataSource()).forceCloseAll();
        if (admin != null) {
            try (Connection cleanupConnection = admin) {
                if (created && database.matches("javaweb_test_[a-f0-9]{32}")) {
                    try (Statement statement = cleanupConnection.createStatement()) { statement.executeUpdate("DROP DATABASE " + database); }
                    System.out.println("Removed isolated schema: " + database);
                }
            }
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(settings.getProperty("db.url"), settings.getProperty("db.username"), settings.getProperty("db.password"));
    }

    @Test
    void returnedHistoryBlocksDeletionAndRepeatedReturn() {
        LoanService loans = new LoanService(factory);
        loans.borrow(bookId, studentId);
        long loanId = loans.findAll().get(0).getId();
        loans.returnBook(loanId);
        assertThrows(BusinessException.class, () -> loans.returnBook(loanId));
        assertThrows(BusinessException.class, () -> new BookService(factory).delete(bookId));
        assertTrue(new BookService(factory).search("", "available").get(0).isLoanHistory());
        assertEquals(1, loans.search("returned").size());
        assertTrue(loans.search("active").isEmpty());
        // The database itself must also protect the history when the service is bypassed.
        try (var session = factory.openSession(false)) {
            assertThrows(RuntimeException.class, () -> session.getMapper(BookMapper.class).delete(bookId));
            session.rollback();
        }
    }

    @Test
    void concurrentBorrowHasExactlyOneWinner() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> borrow = () -> {
            start.await();
            try { new LoanService(factory).borrow(bookId, studentId); return true; }
            catch (BusinessException expected) { return false; }
        };
        try {
            Future<Boolean> first = executor.submit(borrow);
            Future<Boolean> second = executor.submit(borrow);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(1, new LoanService(factory).findAll().size());
            assertTrue(new BookService(factory).findAvailable().isEmpty());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    @Test
    void failedBookUpdateRollsBackInsertedLoan() throws Exception {
        // Fail the second write using a trigger in the disposable schema only.
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER fail_availability BEFORE UPDATE ON books FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'integration failure'");
        }
        try {
            assertThrows(RuntimeException.class, () -> new LoanService(factory).borrow(bookId, studentId));
            assertTrue(new LoanService(factory).findAll().isEmpty());
            assertEquals(1, new BookService(factory).findAvailable().size());
        } finally {
            try (Connection connection = connection(); Statement statement = connection.createStatement()) {
                statement.execute("DROP TRIGGER fail_availability");
            }
        }
    }

    @Test
    void searchTreatsWildcardsLiterallyAndFiltersAvailability() {
        BookService books = new BookService(factory);
        books.create("literal-%_!", "100%_! Java", "作者");
        assertEquals(1, books.search("%_!", "").size());
        assertTrue(books.search("' OR 1=1 --", "").isEmpty());
        assertEquals(1, new StudentService(factory).search("T01").size());
        new LoanService(factory).borrow(bookId, studentId);
        assertEquals(1, books.search("", "borrowed").size());
        assertEquals(1, books.search("", "available").size());
    }

    @Test
    void duplicateIsbnAndDeletionWithoutHistory() {
        BookService books = new BookService(factory);
        assertThrows(BusinessException.class, () -> books.create("test-isbn", "重复", "作者"));
        books.delete(bookId);
        assertTrue(books.findAll().isEmpty());
    }
}
