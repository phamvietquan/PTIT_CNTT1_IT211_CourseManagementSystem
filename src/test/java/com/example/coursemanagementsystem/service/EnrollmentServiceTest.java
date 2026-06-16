package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.EnrollCourseRequest;
import com.example.coursemanagementsystem.model.dto.response.EnrollmentResponse;
import com.example.coursemanagementsystem.model.entity.Course;
import com.example.coursemanagementsystem.model.entity.Enrollment;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho EnrollmentService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User mockStudent;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockStudent = User.builder()
                .id(1L)
                .username("student01")
                .fullName("Nguyễn Văn Sinh")
                .email("student@ptit.edu.vn")
                .role(Role.STUDENT)
                .active(true)
                .build();

        mockCourse = Course.builder()
                .id(10L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .maxStudents(50)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("enrollCourse - Thành công khi tất cả điều kiện hợp lệ")
    void enrollCourse_validRequest_success() {
        // Arrange
        EnrollCourseRequest request = new EnrollCourseRequest(10L);

        Enrollment savedEnrollment = Enrollment.builder()
                .id(100L)
                .student(mockStudent)
                .course(mockCourse)
                .build();

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.countByCourseId(10L)).thenReturn(0L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

        // Act
        EnrollmentResponse response = enrollmentService.enrollCourse("student01", request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCourseCode()).isEqualTo("CS101");
        assertThat(response.getStudentName()).isEqualTo("Nguyễn Văn Sinh");
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("enrollCourse - Khóa học inactive -> ném BusinessException")
    void enrollCourse_inactiveCourse_throwsBusinessException() {
        // Arrange
        Course inactiveCourse = Course.builder()
                .id(10L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .maxStudents(50)
                .active(false) // đã vô hiệu hóa
                .build();

        EnrollCourseRequest request = new EnrollCourseRequest(10L);

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(inactiveCourse));

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enrollCourse("student01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vô hiệu hóa");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("enrollCourse - Đã đăng ký rồi -> ném DuplicateResourceException")
    void enrollCourse_alreadyEnrolled_throwsDuplicateResourceException() {
        // Arrange
        EnrollCourseRequest request = new EnrollCourseRequest(10L);

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enrollCourse("student01", request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã đăng ký");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("enrollCourse - Khóa học đầy -> ném BusinessException")
    void enrollCourse_courseFull_throwsBusinessException() {
        // Arrange
        Course fullCourse = Course.builder()
                .id(10L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .maxStudents(2)
                .active(true)
                .build();

        EnrollCourseRequest request = new EnrollCourseRequest(10L);

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(fullCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.countByCourseId(10L)).thenReturn(2L); // đã đạt max

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.enrollCourse("student01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã đầy");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("getMyEnrollments - User không tồn tại -> ném ResourceNotFoundException")
    void getMyEnrollments_unknownUser_throwsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> enrollmentService.getMyEnrollments("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(enrollmentRepository, never()).findByStudentId(anyLong());
    }
}
