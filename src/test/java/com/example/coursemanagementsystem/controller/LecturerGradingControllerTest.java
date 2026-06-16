package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.GradeSubmissionRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.SubmissionStatus;
import com.example.coursemanagementsystem.service.GradingService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho LecturerGradingController (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LecturerGradingController Unit Tests")
class LecturerGradingControllerTest {

    @Mock
    private GradingService gradingService;

    @InjectMocks
    private LecturerGradingController lecturerGradingController;

    private Authentication mockAuth;
    private SubmissionResponse mockGradedResponse;

    @BeforeEach
    void setUp() {
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("lecturer01");

        mockGradedResponse = SubmissionResponse.builder()
                .id(200L)
                .assignmentId(50L)
                .assignmentTitle("Bài tập 1")
                .studentId(1L)
                .studentName("Trần Văn Sinh")
                .githubUrl("https://github.com/student/hw1")
                .status(SubmissionStatus.GRADED)
                .score(90)
                .feedback("Xuất sắc!")
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("gradeSubmission - Request hợp lệ -> 200 OK với SubmissionResponse đã chấm")
    void gradeSubmission_validRequest_returns200() {
        // Arrange
        GradeSubmissionRequest request = new GradeSubmissionRequest(200L, 90, "Xuất sắc!");

        when(gradingService.gradeSubmission(eq("lecturer01"), any(GradeSubmissionRequest.class)))
                .thenReturn(mockGradedResponse);

        // Act
        ResponseEntity<ApiResponse<SubmissionResponse>> response =
                lecturerGradingController.gradeSubmission(request, mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(response.getBody().getData().getScore()).isEqualTo(90);
        assertThat(response.getBody().getMessage()).isEqualTo("Chấm điểm thành công");
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("gradeSubmission - Lấy lecturerUsername từ Authentication đúng")
    void gradeSubmission_usesLecturerUsernameFromAuthentication() {
        // Arrange
        GradeSubmissionRequest request = new GradeSubmissionRequest(200L, 75, "Tốt");

        when(gradingService.gradeSubmission(anyString(), any(GradeSubmissionRequest.class)))
                .thenReturn(mockGradedResponse);

        // Act
        lecturerGradingController.gradeSubmission(request, mockAuth);

        // Assert
        verify(gradingService, times(1))
                .gradeSubmission(eq("lecturer01"), any(GradeSubmissionRequest.class));
        verify(mockAuth, times(1)).getName();
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("getSubmissionsForAssignment - Có dữ liệu -> 200 OK với danh sách")
    void getSubmissionsForAssignment_withData_returns200() {
        // Arrange
        List<SubmissionResponse> submissions = List.of(mockGradedResponse);
        when(gradingService.getSubmissionsForAssignment("lecturer01", 50L))
                .thenReturn(submissions);

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                lecturerGradingController.getSubmissionsForAssignment(50L, mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getScore()).isEqualTo(90);
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("getSubmissionsForAssignment - Không có bài nộp -> 200 OK với list rỗng")
    void getSubmissionsForAssignment_empty_returns200WithEmptyList() {
        // Arrange
        when(gradingService.getSubmissionsForAssignment("lecturer01", 50L))
                .thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                lecturerGradingController.getSubmissionsForAssignment(50L, mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("getSubmissionsForAssignment - Message trả về đúng nội dung")
    void getSubmissionsForAssignment_correctSuccessMessage() {
        // Arrange
        when(gradingService.getSubmissionsForAssignment("lecturer01", 50L))
                .thenReturn(List.of(mockGradedResponse));

        // Act
        ResponseEntity<ApiResponse<List<SubmissionResponse>>> response =
                lecturerGradingController.getSubmissionsForAssignment(50L, mockAuth);

        // Assert
        assertThat(response.getBody().getMessage())
                .isEqualTo("Lấy danh sách bài nộp thành công");
    }
}
