package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.EnrollCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.EnrollmentResponse;
import com.example.coursemanagementsystem.service.EnrollmentService;
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
 * Unit Tests cho StudentEnrollmentController (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentEnrollmentController Unit Tests")
class StudentEnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private StudentEnrollmentController studentEnrollmentController;

    private Authentication mockAuth;
    private EnrollmentResponse mockEnrollmentResponse;

    @BeforeEach
    void setUp() {
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("student01");

        mockEnrollmentResponse = EnrollmentResponse.builder()
                .id(100L)
                .studentId(1L)
                .studentName("Nguyễn Văn A")
                .courseId(10L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .enrolledAt(LocalDateTime.now())
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("enrollCourse - Request hợp lệ -> 201 Created với EnrollmentResponse")
    void enrollCourse_validRequest_returns201() {
        // Arrange
        EnrollCourseRequest request = new EnrollCourseRequest(10L);
        when(enrollmentService.enrollCourse(eq("student01"), any(EnrollCourseRequest.class)))
                .thenReturn(mockEnrollmentResponse);

        // Act
        ResponseEntity<ApiResponse<EnrollmentResponse>> response =
                studentEnrollmentController.enrollCourse(request, mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getCourseCode()).isEqualTo("CS101");
        assertThat(response.getBody().getMessage()).isEqualTo("Đăng ký khóa học thành công");
        verify(enrollmentService, times(1))
                .enrollCourse(eq("student01"), any(EnrollCourseRequest.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("enrollCourse - Lấy username từ Authentication đúng")
    void enrollCourse_usesUsernameFromAuthentication() {
        // Arrange
        EnrollCourseRequest request = new EnrollCourseRequest(10L);
        when(enrollmentService.enrollCourse(anyString(), any(EnrollCourseRequest.class)))
                .thenReturn(mockEnrollmentResponse);

        // Act
        studentEnrollmentController.enrollCourse(request, mockAuth);

        // Assert - đảm bảo service được gọi với đúng username từ token
        verify(enrollmentService, times(1))
                .enrollCourse(eq("student01"), any(EnrollCourseRequest.class));
        verify(mockAuth, times(1)).getName();
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("getMyEnrollments - Trả về 200 OK với danh sách enrollment")
    void getMyEnrollments_returnsListWith200() {
        // Arrange
        List<EnrollmentResponse> enrollments = List.of(mockEnrollmentResponse);
        when(enrollmentService.getMyEnrollments("student01")).thenReturn(enrollments);

        // Act
        ResponseEntity<ApiResponse<List<EnrollmentResponse>>> response =
                studentEnrollmentController.getMyEnrollments(mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getCourseCode()).isEqualTo("CS101");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("getMyEnrollments - Không có enrollment nào -> 200 OK với list rỗng")
    void getMyEnrollments_empty_returns200WithEmptyList() {
        // Arrange
        when(enrollmentService.getMyEnrollments("student01")).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponse<List<EnrollmentResponse>>> response =
                studentEnrollmentController.getMyEnrollments(mockAuth);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("getMyEnrollments - Message trả về đúng nội dung")
    void getMyEnrollments_correctSuccessMessage() {
        // Arrange
        when(enrollmentService.getMyEnrollments("student01"))
                .thenReturn(List.of(mockEnrollmentResponse));

        // Act
        ResponseEntity<ApiResponse<List<EnrollmentResponse>>> response =
                studentEnrollmentController.getMyEnrollments(mockAuth);

        // Assert
        assertThat(response.getBody().getMessage())
                .isEqualTo("Lấy danh sách khóa học đã đăng ký thành công");
    }
}
