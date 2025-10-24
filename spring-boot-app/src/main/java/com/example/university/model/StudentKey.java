package com.example.university.model;

import java.io.Serializable;
import java.util.UUID;

// Composite key used for Cassandra partitioning
public class StudentKey implements Serializable {

    private String department;
    private int year;
    private UUID studentId;

    public StudentKey() {}

    public StudentKey(String department, int year, UUID studentId) {
        this.department = department;
        this.year = year;
        this.studentId = studentId;
    }

    // Getters and setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
}
