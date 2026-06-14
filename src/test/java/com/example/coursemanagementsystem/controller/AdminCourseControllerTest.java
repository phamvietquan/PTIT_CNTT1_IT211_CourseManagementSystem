package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.CreateCourseRequest;
import com.example.coursemanagementsystem.model.dto.request.UpdateCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.CourseResponse;
import com.example.coursemanagementsystem.model.dto.response.PageResponse;
import com.example.coursemanagementsystem.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCourseController Unit Tests")
class AdminCourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private AdminCourseController adminCourseController;

    private CourseResponse mockCourse;

    @BeforeEach
    void setUp() {
        mockCourse = CourseResponse.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .description("Môn học lập trình Java cơ bản")
                .maxStudents(50)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("getAllCourses - Không có keyword -> 200 OK với PageResponse")
    void getAllCourses_noKeyword_returns200WithPage() {
        // Arrange
        PageResponse<CourseResponse> mockPage = PageResponse.<CourseResponse>builder()
                .content(List.of(mockCourse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(courseService.getAllCourses(isNull(), any(Pageable.class))).thenReturn(mockPage);

        // Act
        ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> response =
                adminCourseController.getAllCourses(0, 10, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getTotalElements()).isEqualTo(1L);
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("getAllCourses - Có keyword -> 200 OK, gọi service với keyword")
    void getAllCourses_withKeyword_callsServiceWithKeyword() {
        // Arrange
        PageResponse<CourseResponse> mockPage = PageResponse.<CourseResponse>builder()
                .content(List.of(mockCourse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(courseService.getAllCourses(eq("java"), any(Pageable.class))).thenReturn(mockPage);

        // Act
        ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> response =
                adminCourseController.getAllCourses(0, 10, "java");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        verify(courseService, times(1)).getAllCourses(eq("java"), any(Pageable.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("getCourseById - ID tồn tại -> 200 OK với CourseResponse")
    void getCourseById_existingId_returns200() {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(mockCourse);

        // Act
        ResponseEntity<ApiResponse<CourseResponse>> response =
                adminCourseController.getCourseById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getCourseCode()).isEqualTo("CS101");
        assertThat(response.getBody().getData().getCourseName()).isEqualTo("Lập trình Java");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("createCourse - Request hợp lệ -> 201 Created với CourseResponse")
    void createCourse_validRequest_returns201() {
        // Arrange
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS202")
                .courseName("Cơ sở dữ liệu")
                .description("Môn học cơ sở dữ liệu")
                .maxStudents(40)
                .lecturerId(2L)
                .build();

        CourseResponse created = CourseResponse.builder()
                .id(2L)
                .courseCode("CS202")
                .courseName("Cơ sở dữ liệu")
                .active(true)
                .build();

        when(courseService.createCourse(any(CreateCourseRequest.class))).thenReturn(created);

        // Act
        ResponseEntity<ApiResponse<CourseResponse>> response =
                adminCourseController.createCourse(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getCourseCode()).isEqualTo("CS202");
        assertThat(response.getBody().getMessage()).isEqualTo("Tạo khóa học thành công");
        verify(courseService, times(1)).createCourse(any(CreateCourseRequest.class));
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("deactivateCourse - ID tồn tại -> 204 No Content, gọi service.deactivate")
    void deactivateCourse_existingId_returns204() {
        // Arrange
        doNothing().when(courseService).deactivateCourse(1L);

        // Act
        ResponseEntity<Void> response = adminCourseController.deactivateCourse(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(courseService, times(1)).deactivateCourse(1L);
    }
}
