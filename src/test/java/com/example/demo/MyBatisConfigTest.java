package com.example.demo;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MyBatisConfigTest {
    @Test
    void loadsConfigurationAndAllMappersWithoutConnectingToDatabase() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, properties());
            assertNotNull(factory.getConfiguration().getMapperRegistry());
            assertNotNull(factory.getConfiguration().getMappedStatement("com.example.demo.mapper.BookMapper.findAll"));
            assertNotNull(factory.getConfiguration().getMappedStatement("com.example.demo.mapper.LoanMapper.findAll"));
        }
    }
    private java.util.Properties properties() {
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("db.url", "jdbc:mysql://127.0.0.1:1/unused");
        properties.setProperty("db.username", "unused");
        properties.setProperty("db.password", "unused");
        return properties;
    }
}
