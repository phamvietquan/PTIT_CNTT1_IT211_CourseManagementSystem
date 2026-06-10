package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.CreateCourseRequest;
import com.example.coursemanagementsystem.model.dto.request.UpdateCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.CourseResponse;
import com.example.coursemanagementsystem.model.dto.response.PageResponse;
import com.example.coursemanagementsystem.model.entity.Course;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCourses(String keyword, Pageable pageable) {
        Page<Course> coursePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            coursePage = courseRepository.findByCourseNameContainingIgnoreCase(keyword, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }

        Page<CourseResponse> dtoPage = coursePage.map(CourseResponse::fromEntity);
        return PageResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", request.getCourseCode());
        }

        User lecturer = userRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", request.getLecturerId()));

        if (lecturer.getRole() != Role.LECTURER) {
            throw new BusinessException("User với ID " + request.getLecturerId()
                    + " không phải là Giảng viên (LECTURER)");
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 50)
                .lecturer(lecturer)
                .active(true)
                .build();

        Course savedCourse = courseRepository.save(course);
        return CourseResponse.fromEntity(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getMaxStudents() != null) {
            course.setMaxStudents(request.getMaxStudents());
        }
        if (request.getLecturerId() != null) {
            User lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lecturer", "id", request.getLecturerId()));
            if (lecturer.getRole() != Role.LECTURER) {
                throw new BusinessException("User với ID " + request.getLecturerId()
                        + " không phải là Giảng viên (LECTURER)");
            }
            course.setLecturer(lecturer);
        }
        if (request.getActive() != null) {
            course.setActive(request.getActive());
        }

        Course updatedCourse = courseRepository.save(course);
        return CourseResponse.fromEntity(updatedCourse);
    }

    @Transactional
    public void deactivateCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setActive(false);
        courseRepository.save(course);
    }
}
