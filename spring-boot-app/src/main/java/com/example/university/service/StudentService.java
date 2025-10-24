package com.example.university.service;

import com.example.university.model.Student;
import java.util.Optional;
import java.util.UUID;

public interface StudentService {

    Student createStudent(Student student);

    Optional<Student> getStudentById(UUID studentId);

    Student updateStudent(Student student);

    void deleteStudent(UUID studentId);
}
