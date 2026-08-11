package com.example.demo.mapper;

import com.example.demo.domain.Student;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper {
    @Select("SELECT id, student_no, name, class_name FROM students ORDER BY student_no")
    List<Student> findAll();

    @Select("SELECT id, student_no, name, class_name FROM students WHERE id = #{id}")
    Student findById(Long id);
}
