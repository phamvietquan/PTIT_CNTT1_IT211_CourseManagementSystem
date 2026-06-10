package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.model.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCode(String courseCode);

    Page<Course> findByCourseNameContainingIgnoreCase(String keyword, Pageable pageable);
}
