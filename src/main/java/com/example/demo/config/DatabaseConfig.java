package com.example.demo.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Loads local configuration first; explicitly supplied environment variables take precedence. */
public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static Properties load() throws IOException {
        String file = System.getenv("JAVAWEB_DB_CONFIG");
        Path path = Path.of(file == null || file.isBlank() ? "config/database.local.properties" : file);
        return load(path, System.getenv());
    }

    static Properties load(Path path, Map<String, String> environment) throws IOException {
        Properties properties = new Properties();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } else if (environment.containsKey("JAVAWEB_DB_CONFIG")) {
            throw new IOException("找不到 JAVAWEB_DB_CONFIG 指定的配置文件");
        }
        Map<String, String> keys = Map.of("db.url", "JAVAWEB_DB_URL",
                "db.username", "JAVAWEB_DB_USERNAME", "db.password", "JAVAWEB_DB_PASSWORD");
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            String value = environment.get(entry.getValue());
            if (value != null) properties.setProperty(entry.getKey(), value);
            if (properties.getProperty(entry.getKey(), "").isBlank()) {
                throw new IllegalStateException("缺少数据库配置 " + entry.getKey()
                        + "，请配置本地 properties 文件或 " + entry.getValue());
            }
        }
        return properties;
    }

    public static SqlSessionFactory createFactory(Properties properties) throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(reader, properties);
        }
    }
}
