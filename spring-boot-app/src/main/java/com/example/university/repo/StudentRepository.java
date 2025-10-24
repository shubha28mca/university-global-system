package com.example.university.repo;

import com.example.university.model.Student;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends CassandraRepository<Student, UUID> { // below are optional if you are not goving also this will work fine

    /**
     * Find all students in a given department and admission year.
     * Uses the partition key (department, year) for efficient query.
     */
    @Query("SELECT * FROM student WHERE department = ?0 AND year = ?1")
    List<Student> findByDepartmentAndYear(String department, int year);

    /**
     * Find a student by department, year, and student ID.
     * Fully qualifies the primary key (partition + clustering) for efficient lookup.
     */
    @Query("SELECT * FROM student WHERE department = ?0 AND year = ?1 AND student_id = ?2")
    Student findByDepartmentYearAndId(String department, int year, UUID studentId);

    /**
     * Find students by department and partial name match.
     * Note: Cassandra is not efficient for LIKE queries; only for demonstration.
     * For production, consider secondary indexes or full-text search (e.g., Elasticsearch).
     */
    @Query("SELECT * FROM student WHERE department = ?0 AND year = ?1 ALLOW FILTERING")
    List<Student> findByDepartmentYearAndNameLike(String department, int year, String namePattern);

    /**
     * Find all students across all years in a department.
     * Must use ALLOW FILTERING, as year is part of the partition key.
     * Use sparingly in production; better to create a materialized view if frequent.
     */
    @Query("SELECT * FROM student WHERE department = ?0 ALLOW FILTERING")
    List<Student> findAllByDepartment(String department);
}
