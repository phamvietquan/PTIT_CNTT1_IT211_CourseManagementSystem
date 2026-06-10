package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/submissions")
public class StudentSubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        SubmissionResponse response = submissionService.submitAssignment(
                username, assignmentId, githubUrl, file);
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
