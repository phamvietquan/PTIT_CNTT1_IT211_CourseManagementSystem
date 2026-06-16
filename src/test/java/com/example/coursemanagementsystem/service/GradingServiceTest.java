package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.GradeSubmissionRequest;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.*;
import com.example.coursemanagementsystem.repository.SubmissionRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho GradingService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GradingService Unit Tests")
class GradingServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GradingService gradingService;

    private User mockLecturer;
    private User mockStudent;
    private Course mockCourse;
    private Assignment mockAssignment;
    private Submission mockSubmission;

    @BeforeEach
    void setUp() {
        mockLecturer = User.builder()
                .id(10L)
                .username("lecturer01")
                .fullName("Nguyễn Văn Giảng")
                .role(Role.LECTURER)
                .active(true)
                .build();

        mockStudent = User.builder()
                .id(1L)
                .username("student01")
                .fullName("Trần Văn Sinh")
                .role(Role.STUDENT)
                .active(true)
                .build();

        mockCourse = Course.builder()
                .id(100L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .lecturer(mockLecturer)
                .active(true)
                .build();

        mockAssignment = Assignment.builder()
                .id(50L)
                .title("Bài tập 1: Hello World")
                .course(mockCourse)
                .build();

        mockSubmission = Submission.builder()
                .id(200L)
                .assignment(mockAssignment)
                .student(mockStudent)
                .githubUrl("https://github.com/student/hw1")
                .status(SubmissionStatus.SUBMITTED)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("gradeSubmission - Chấm điểm hợp lệ -> cập nhật status thành GRADED")
    void gradeSubmission_validRequest_success() {
        // Arrange
        GradeSubmissionRequest request = new GradeSubmissionRequest(200L, 85, "Làm tốt lắm!");

        Submission gradedSubmission = Submission.builder()
                .id(200L)
                .assignment(mockAssignment)
                .student(mockStudent)
                .githubUrl("https://github.com/student/hw1")
                .status(SubmissionStatus.GRADED)
                .score(85)
                .feedback("Làm tốt lắm!")
                .build();

        when(userRepository.findByUsername("lecturer01")).thenReturn(Optional.of(mockLecturer));
        when(submissionRepository.findById(200L)).thenReturn(Optional.of(mockSubmission));
        when(submissionRepository.save(any(Submission.class))).thenReturn(gradedSubmission);

        // Act
        SubmissionResponse response = gradingService.gradeSubmission("lecturer01", request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(response.getScore()).isEqualTo(85);
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("gradeSubmission - Lecturer không phải của khóa học -> ném BusinessException")
    void gradeSubmission_wrongLecturer_throwsBusinessException() {
        // Arrange
        User otherLecturer = User.builder()
                .id(99L)
                .username("lecturer99")
                .role(Role.LECTURER)
                .active(true)
                .build();

        GradeSubmissionRequest request = new GradeSubmissionRequest(200L, 90, "");

        when(userRepository.findByUsername("lecturer99")).thenReturn(Optional.of(otherLecturer));
        when(submissionRepository.findById(200L)).thenReturn(Optional.of(mockSubmission));

        // Act & Assert
        assertThatThrownBy(() -> gradingService.gradeSubmission("lecturer99", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không phải giảng viên của khóa học");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("gradeSubmission - Bài nộp ở trạng thái PENDING -> ném BusinessException")
    void gradeSubmission_pendingSubmission_throwsBusinessException() {
        // Arrange
        Submission pendingSubmission = Submission.builder()
                .id(201L)
                .assignment(mockAssignment)
                .student(mockStudent)
                .status(SubmissionStatus.PENDING) // sinh viên chưa nộp
                .build();

        GradeSubmissionRequest request = new GradeSubmissionRequest(201L, 70, "");

        when(userRepository.findByUsername("lecturer01")).thenReturn(Optional.of(mockLecturer));
        when(submissionRepository.findById(201L)).thenReturn(Optional.of(pendingSubmission));

        // Act & Assert
        assertThatThrownBy(() -> gradingService.gradeSubmission("lecturer01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa nộp bài");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("gradeSubmission - Bài đã được chấm (GRADED) -> ném BusinessException")
    void gradeSubmission_alreadyGraded_throwsBusinessException() {
        // Arrange
        Submission gradedSubmission = Submission.builder()
                .id(202L)
                .assignment(mockAssignment)
                .student(mockStudent)
                .status(SubmissionStatus.GRADED) // đã chấm rồi
                .score(80)
                .build();

        GradeSubmissionRequest request = new GradeSubmissionRequest(202L, 95, "Chấm lại");

        when(userRepository.findByUsername("lecturer01")).thenReturn(Optional.of(mockLecturer));
        when(submissionRepository.findById(202L)).thenReturn(Optional.of(gradedSubmission));

        // Act & Assert
        assertThatThrownBy(() -> gradingService.gradeSubmission("lecturer01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã được chấm điểm rồi");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("gradeSubmission - Submission ID không tồn tại -> ném ResourceNotFoundException")
    void gradeSubmission_submissionNotFound_throwsResourceNotFoundException() {
        // Arrange
        GradeSubmissionRequest request = new GradeSubmissionRequest(9999L, 80, "");

        when(userRepository.findByUsername("lecturer01")).thenReturn(Optional.of(mockLecturer));
        when(submissionRepository.findById(9999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gradingService.gradeSubmission("lecturer01", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Submission");
    }
}
