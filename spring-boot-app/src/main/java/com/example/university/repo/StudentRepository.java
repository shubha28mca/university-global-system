package com.example.university.repo;

import com.example.university.model.Student;
import com.example.university.model.StudentKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentRepository extends CassandraRepository<Student, UUID> {
    // You can add custom queries if needed, e.g., find by department & year
}
