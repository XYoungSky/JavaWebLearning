package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {
    @TempDir Path directory;

    @Test
    void environmentOverridesLocalFileWithoutLosingOtherValues() throws Exception {
        Path file = directory.resolve("database.properties");
        Files.writeString(file, "db.url=jdbc:mysql://localhost/study\ndb.username=local\ndb.password=本地密码\n");
        var properties = DatabaseConfig.load(file, Map.of("JAVAWEB_DB_USERNAME", "environment"));
        assertEquals("environment", properties.getProperty("db.username"));
        assertEquals("本地密码", properties.getProperty("db.password"));
    }

    @Test
    void missingSettingsFailWithActionableMessage() {
        var error = assertThrows(IllegalStateException.class,
                () -> DatabaseConfig.load(directory.resolve("missing"), Map.of()));
        assertTrue(error.getMessage().contains("缺少数据库配置"));
    }

    @Test
    void explicitMissingFileDoesNotSilentlyFallBack() {
        assertThrows(java.io.IOException.class, () -> DatabaseConfig.load(directory.resolve("missing"),
                Map.of("JAVAWEB_DB_CONFIG", "missing")));
    }
}
