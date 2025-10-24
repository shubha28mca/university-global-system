package com.example.university.model;

import java.util.UUID;

// Represents a student record stored in Cassandra
public class Student {

    private UUID studentId;     // Unique student ID (primary key)
    private String department;  // Department or course
    private int year;           // Admission year
    private String name;        // Student name

    public Student() {}

    public Student(UUID studentId, String department, int year, String name) {
        this.studentId = studentId;
        this.department = department;
        this.year = year;
        this.name = name;
    }

    // Getters and setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
