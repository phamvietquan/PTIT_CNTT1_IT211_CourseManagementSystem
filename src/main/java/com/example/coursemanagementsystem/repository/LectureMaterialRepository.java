package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.model.entity.LectureMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {

    List<LectureMaterial> findByCourseId(Long courseId);
}
