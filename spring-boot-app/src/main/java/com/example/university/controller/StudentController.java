package com.example.university.controller;

import com.example.university.model.Student;
import com.example.university.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService service;

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        if (student.getStudentId() == null) student.setStudentId(UUID.randomUUID());
        return service.createStudent(student);
    }

    @GetMapping("/{id}")
    public Optional<Student> getStudent(@PathVariable("id") UUID studentId) {
        return service.getStudentById(studentId);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable("id") UUID studentId, @RequestBody Student student) {
        student.setStudentId(studentId);
        return service.updateStudent(student);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable("id") UUID studentId) {
        service.deleteStudent(studentId);
    }
}
