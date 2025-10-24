package com.example.university.service;

import com.example.university.model.Student;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    Student getStudentById(String department, int year, UUID studentId);

    List<Student> getStudentsByDepartmentAndYear(String department, int year);

    List<Student> getStudentsByName(String department, int year, String namePattern);

    List<Student> getAllStudentsByDepartment(String department);

    Student createOrUpdateStudent(Student student);

    void deleteStudent(String department, int year, UUID studentId);
}
// this will work with the new Query based Repo
