package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.SubmitAssignmentRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/submissions")
public class StudentSubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @Valid @RequestBody SubmitAssignmentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SubmissionResponse response = submissionService.submitAssignment(username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nộp bài thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions(
            Authentication authentication) {
        String username = authentication.getName();
        List<SubmissionResponse> submissions = submissionService.getMySubmissions(username);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách bài nộp thành công", submissions));
    }
}
