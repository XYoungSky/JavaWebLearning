package com.example.demo.mapper;

import com.example.demo.domain.Student;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {
    @Select("SELECT id, student_no, name, class_name FROM students ORDER BY student_no")
    List<Student> findAll();

    @Select("SELECT id, student_no, name, class_name FROM students "
            + "WHERE student_no LIKE #{pattern} ESCAPE '!' OR name LIKE #{pattern} ESCAPE '!' "
            + "OR class_name LIKE #{pattern} ESCAPE '!' ORDER BY student_no")
    List<Student> search(@Param("pattern") String pattern);

    @Select("SELECT id, student_no, name, class_name FROM students WHERE id = #{id}")
    Student findById(Long id);
}
