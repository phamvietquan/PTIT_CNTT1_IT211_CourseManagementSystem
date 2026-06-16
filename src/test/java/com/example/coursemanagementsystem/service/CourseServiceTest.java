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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho CourseService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private User mockLecturer;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockLecturer = User.builder()
                .id(10L)
                .username("lecturer01")
                .email("lecturer@ptit.edu.vn")
                .fullName("Nguyễn Văn Giảng")
                .role(Role.LECTURER)
                .active(true)
                .build();

        mockCourse = Course.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .description("Môn học lập trình Java cơ bản")
                .maxStudents(50)
                .lecturer(mockLecturer)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("createCourse - Trùng courseCode -> ném DuplicateResourceException")
    void createCourse_duplicateCourseCode_throwsDuplicateResourceException() {
        // Arrange
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS101")
                .courseName("Lập trình Java 2")
                .lecturerId(10L)
                .build();

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CS101");

        verify(courseRepository, never()).save(any(Course.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("createCourse - LecturerId là STUDENT -> ném BusinessException")
    void createCourse_lecturerIsStudent_throwsBusinessException() {
        // Arrange
        User student = User.builder()
                .id(5L)
                .username("student01")
                .role(Role.STUDENT)
                .active(true)
                .build();

        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS999")
                .courseName("Test Course")
                .lecturerId(5L)
                .build();

        when(courseRepository.existsByCourseCode("CS999")).thenReturn(false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không phải là Giảng viên");

        verify(courseRepository, never()).save(any(Course.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("createCourse - Request hợp lệ -> tạo course thành công")
    void createCourse_validRequest_success() {
        // Arrange
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS202")
                .courseName("Cơ sở dữ liệu")
                .description("Môn học CSDL")
                .maxStudents(40)
                .lecturerId(10L)
                .build();

        Course savedCourse = Course.builder()
                .id(2L)
                .courseCode("CS202")
                .courseName("Cơ sở dữ liệu")
                .description("Môn học CSDL")
                .maxStudents(40)
                .lecturer(mockLecturer)
                .active(true)
                .build();

        when(courseRepository.existsByCourseCode("CS202")).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(mockLecturer));
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        // Act
        CourseResponse response = courseService.createCourse(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCourseCode()).isEqualTo("CS202");
        assertThat(response.getCourseName()).isEqualTo("Cơ sở dữ liệu");
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("getCourseById - ID không tồn tại -> ném ResourceNotFoundException")
    void getCourseById_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course");
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("deactivateCourse - ID tồn tại -> đặt active=false và lưu")
    void deactivateCourse_existingId_setsActiveFalse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(mockCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);

        // Act
        courseService.deactivateCourse(1L);

        // Assert
        assertThat(mockCourse.getActive()).isFalse();
        verify(courseRepository, times(1)).save(mockCourse);
    }
}
