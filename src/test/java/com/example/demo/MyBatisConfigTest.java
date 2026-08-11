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
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
            assertNotNull(factory.getConfiguration().getMapperRegistry());
            assertNotNull(factory.getConfiguration().getMappedStatement("com.example.demo.mapper.BookMapper.findAll"));
            assertNotNull(factory.getConfiguration().getMappedStatement("com.example.demo.mapper.LoanMapper.findAll"));
        }
    }
}
