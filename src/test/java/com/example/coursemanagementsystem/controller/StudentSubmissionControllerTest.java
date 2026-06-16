package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.SubmitAssignmentRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.SubmissionStatus;
import com.example.coursemanagementsystem.service.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho StudentSubmissionController (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentSubmissionController Unit Tests")
class StudentSubmissionControllerTest {

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private StudentSubmissionController studentSubmissionController;

    private Authentication mockAuth;
    private SubmissionResponse mockSubmissionResponse;

    @BeforeEach
    void setUp() {
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("student01");

        mockSubmissionResponse = SubmissionResponse.builder()
                .id(300L)
                .assignmentId(50L)
                .assignmentTitle("Bài tập 1")
                .studentId(1L)
                .studentName("Lê Văn A")
                .githubUrl("https://github.com/student/hw1")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("submitAssignment - Nộp bài hợp lệ -> 201 Created với SubmissionResponse")
    void submitAssignment_validRequest_returns201() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                50L, "https://github.com/student/hw1");

        when(submissionService.submitAssignment(eq("student01"), any(SubmitAssignmentRequest.class)))
                .thenReturn(mockSubmissionResponse);

        // Act
        ResponseEntity<ApiResponse<SubmissionResponse>> response =
                studentSubmissionController.submitAssignment(request, mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.getBody().getMessage()).isEqualTo("Nộp bài thành công");
        verify(submissionService, times(1))
                .submitAssignment(eq("student01"), any(SubmitAssignmentRequest.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("submitAssignment - Lấy username từ Authentication đúng")
    void submitAssignment_usesUsernameFromAuthentication() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                50L, "https://github.com/student/hw1");

        when(submissionService.submitAssignment(anyString(), any(SubmitAssignmentRequest.class)))
                .thenReturn(mockSubmissionResponse);

        // Act
        studentSubmissionController.submitAssignment(request, mockAuth);

        // Assert
        verify(submissionService, times(1))
                .submitAssignment(eq("student01"), any(SubmitAssignmentRequest.class));
        verify(mockAuth, times(1)).getName();
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("getMySubmissions - Có dữ liệu -> 200 OK với danh sách")
    void getMySubmissions_withData_returns200() {
        // Arrange
        List<SubmissionResponse> submissions = List.of(mockSubmissionResponse);
        when(submissionService.getMySubmissions("student01")).thenReturn(submissions);

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                studentSubmissionController.getMySubmissions(mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getGithubUrl())
                .isEqualTo("https://github.com/student/hw1");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("getMySubmissions - Không có bài nộp -> 200 OK với list rỗng")
    void getMySubmissions_empty_returns200WithEmptyList() {
        // Arrange
        when(submissionService.getMySubmissions("student01")).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                studentSubmissionController.getMySubmissions(mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("getMySubmissions - Message trả về đúng nội dung")
    void getMySubmissions_correctSuccessMessage() {
        // Arrange
        when(submissionService.getMySubmissions("student01"))
                .thenReturn(List.of(mockSubmissionResponse));

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                studentSubmissionController.getMySubmissions(mockAuth);

        // Assert
        assertThat(response.getBody().getMessage())
                .isEqualTo("Lấy danh sách bài nộp thành công");
    }
}
