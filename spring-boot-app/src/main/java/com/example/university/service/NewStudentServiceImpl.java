package com.example.university.service;

import com.example.university.model.Student;
import com.example.university.repo.StudentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    private static final String CACHE_PREFIX = "student:"; // Redis key prefix
    private static final String KAFKA_TOPIC = "student-cache-invalidation";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // =================== GET STUDENT BY ID ===================
    @Override
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackGetStudentById")
    public Student getStudentById(String department, int year, UUID studentId) {
        String cacheKey = CACHE_PREFIX + department + ":" + year + ":" + studentId;

        // Try fetching from Redis cache
        Student cachedStudent = (Student) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStudent != null) {
            return cachedStudent;
        }

        // Fetch from Cassandra
        Student student = studentRepository.findByDepartmentYearAndId(department, year, studentId);
        if (student != null) {
            // Cache locally with TTL
            redisTemplate.opsForValue().set(cacheKey, student, Duration.ofHours(1));
        }
        return student;
    }

    public Student fallbackGetStudentById(String department, int year, UUID studentId, Throwable t) {
        // Circuit breaker fallback: return null or optional cached stale value
        return (Student) redisTemplate.opsForValue().get(CACHE_PREFIX + department + ":" + year + ":" + studentId);
    }

    // =================== GET STUDENTS BY DEPARTMENT + YEAR ===================
    @Override
    public List<Student> getStudentsByDepartmentAndYear(String department, int year) {
        String cacheKey = CACHE_PREFIX + department + ":" + year + ":all";
        List<Student> cached = (List<Student>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        List<Student> students = studentRepository.findByDepartmentAndYear(department, year);
        redisTemplate.opsForValue().set(cacheKey, students, Duration.ofHours(1));
        return students;
    }

    // =================== GET STUDENTS BY NAME PATTERN ===================
    @Override
    public List<Student> getStudentsByName(String department, int year, String namePattern) {
        return studentRepository.findByDepartmentYearAndNameLike(department, year, namePattern);
    }

    // =================== GET ALL STUDENTS BY DEPARTMENT ===================
    @Override
    public List<Student> getAllStudentsByDepartment(String department) {
        return studentRepository.findAllByDepartment(department);
    }

    // =================== CREATE OR UPDATE STUDENT ===================
    @Override
    public Student createOrUpdateStudent(Student student) {
        Student saved = studentRepository.save(student);

        // Update local cache
        String cacheKey = CACHE_PREFIX + student.getDepartment() + ":" + student.getYear() + ":" + student.getStudentId();
        redisTemplate.opsForValue().set(cacheKey, saved, Duration.ofHours(1));

        // Publish cache invalidation to other regions
        kafkaTemplate.send(KAFKA_TOPIC, student.getDepartment() + ":" + student.getYear() + ":" + student.getStudentId());

        return saved;
    }

    // =================== DELETE STUDENT ===================
    @Override
    public void deleteStudent(String department, int year, UUID studentId) {
        studentRepository.deleteById(studentId);

        // Remove from local cache
        String cacheKey = CACHE_PREFIX + department + ":" + year + ":" + studentId;
        redisTemplate.delete(cacheKey);

        // Publish cache invalidation to other regions
        kafkaTemplate.send(KAFKA_TOPIC, department + ":" + year + ":" + studentId);
    }
}
