package com.example.university.service;

import com.example.university.model.Student;
import com.example.university.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    private static final String STUDENT_CACHE_PREFIX = "student:";

    @Autowired
    private StudentRepository repository;

    @Autowired
    private RedisTemplate<String, Student> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String CACHE_INVALIDATION_TOPIC = "student-cache-invalidation";

    // Create a student and cache it
    @Override
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackCreateStudent")
    public Student createStudent(Student student) {
        Student saved = repository.save(student);
        String key = STUDENT_CACHE_PREFIX + student.getStudentId();
        redisTemplate.opsForValue().set(key, saved, Duration.ofMinutes(10));

        // Send Kafka message to invalidate caches in other regions
        kafkaTemplate.send(CACHE_INVALIDATION_TOPIC, student.getStudentId().toString());
        return saved;
    }

    // Fallback if Cassandra or Redis fails
    private Student fallbackCreateStudent(Student student, Throwable t) {
        // Could return a partial object or throw custom exception
        System.out.println("Fallback triggered for createStudent: " + t.getMessage());
        return student;
    }

    // Get student by ID with Redis caching
    @Override
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackGetStudent")
    public Optional<Student> getStudentById(UUID studentId) {
        String key = STUDENT_CACHE_PREFIX + studentId;
        Student cached = redisTemplate.opsForValue().get(key);
        if (cached != null) return Optional.of(cached);

        Optional<Student> student = repository.findById(studentId);
        student.ifPresent(s -> redisTemplate.opsForValue().set(key, s, Duration.ofMinutes(10)));
        return student;
    }

    private Optional<Student> fallbackGetStudent(UUID studentId, Throwable t) {
        System.out.println("Fallback triggered for getStudentById: " + t.getMessage());
        // Try returning stale cache if available
        Student cached = redisTemplate.opsForValue().get(STUDENT_CACHE_PREFIX + studentId);
        return Optional.ofNullable(cached);
    }

    @Override
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackUpdateStudent")
    public Student updateStudent(Student student) {
        Student updated = repository.save(student);
        String key = STUDENT_CACHE_PREFIX + student.getStudentId();
        redisTemplate.opsForValue().set(key, updated, Duration.ofMinutes(10));
        kafkaTemplate.send(CACHE_INVALIDATION_TOPIC, student.getStudentId().toString());
        return updated;
    }

    private Student fallbackUpdateStudent(Student student, Throwable t) {
        System.out.println("Fallback triggered for updateStudent: " + t.getMessage());
        return student;
    }

    @Override
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackDeleteStudent")
    public void deleteStudent(UUID studentId) {
        repository.deleteById(studentId);
        redisTemplate.delete(STUDENT_CACHE_PREFIX + studentId);
        kafkaTemplate.send(CACHE_INVALIDATION_TOPIC, studentId.toString());
    }

    private void fallbackDeleteStudent(UUID studentId, Throwable t) {
        System.out.println("Fallback triggered for deleteStudent: " + t.getMessage());
    }
}
