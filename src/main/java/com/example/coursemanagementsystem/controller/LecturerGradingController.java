package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.GradeSubmissionRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.service.GradingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturer")
public class LecturerGradingController {

    @Autowired
    private GradingService gradingService;

    @PostMapping("/grades")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @Valid @RequestBody GradeSubmissionRequest request,
            Authentication authentication) {
        String lecturerUsername = authentication.getName();
        SubmissionResponse response = gradingService.gradeSubmission(
                lecturerUsername, request);
        return ResponseEntity.ok(
                ApiResponse.success("Chấm điểm thành công", response));
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsForAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            Authentication authentication) {
        String lecturerUsername = authentication.getName();
        List<SubmissionResponse> submissions = gradingService.getSubmissionsForAssignment(
                lecturerUsername, assignmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách bài nộp thành công", submissions));
    }
}
