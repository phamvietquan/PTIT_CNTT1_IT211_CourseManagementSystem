package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.EnrollCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.EnrollmentResponse;
import com.example.coursemanagementsystem.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/enrollments")
public class StudentEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourse(
            @Valid @RequestBody EnrollCourseRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        EnrollmentResponse enrollment = enrollmentService.enrollCourse(username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khóa học thành công", enrollment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            Authentication authentication) {
        String username = authentication.getName();
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(username);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khóa học đã đăng ký thành công", enrollments));
    }
}
