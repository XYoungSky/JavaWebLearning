package com.example.demo.service;

import com.example.demo.domain.Student;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.util.Search;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class StudentService {
    private final SqlSessionFactory factory;

    public StudentService(SqlSessionFactory factory) { this.factory = factory; }

    public List<Student> search(String keyword) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(StudentMapper.class).search(Search.pattern(Search.keyword(keyword)));
        }
    }

    public List<Student> findAll() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(StudentMapper.class).findAll();
        }
    }
}
