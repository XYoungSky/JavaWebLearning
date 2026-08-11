package com.example.demo.domain;

public class Student {
    private Long id;
    private String studentNo;
    private String name;
    private String className;

    public Student() {}

    public Student(String studentNo, String name, String className) {
        this.studentNo = studentNo;
        this.name = name;
        this.className = className;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
