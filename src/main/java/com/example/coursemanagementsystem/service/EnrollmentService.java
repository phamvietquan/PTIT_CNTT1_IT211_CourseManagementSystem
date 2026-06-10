package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.EnrollCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.EnrollmentResponse;
import com.example.coursemanagementsystem.model.entity.Course;
import com.example.coursemanagementsystem.model.entity.Enrollment;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public EnrollmentResponse enrollCourse(String username, EnrollCourseRequest request) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", request.getCourseId()));

        if (!course.getActive()) {
            throw new BusinessException("Khóa học '" + course.getCourseName()
                    + "' đã bị vô hiệu hóa, không thể đăng ký");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException(
                    "Bạn đã đăng ký khóa học '" + course.getCourseName() + "' rồi");
        }

        if (course.getMaxStudents() != null) {
            long currentCount = enrollmentRepository.countByCourseId(course.getId());
            if (currentCount >= course.getMaxStudents()) {
                throw new BusinessException("Khóa học '" + course.getCourseName()
                        + "' đã đầy (" + course.getMaxStudents() + " sinh viên)");
            }
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(savedEnrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        return enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
