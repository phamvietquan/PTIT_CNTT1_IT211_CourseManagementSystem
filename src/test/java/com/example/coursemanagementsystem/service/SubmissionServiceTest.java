package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.SubmitAssignmentRequest;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.*;
import com.example.coursemanagementsystem.repository.AssignmentRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.SubmissionRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho SubmissionService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionService Unit Tests")
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private User mockStudent;
    private Course mockCourse;
    private Assignment mockAssignment;

    @BeforeEach
    void setUp() {
        mockStudent = User.builder()
                .id(1L)
                .username("student01")
                .fullName("Lê Văn Sinh")
                .role(Role.STUDENT)
                .active(true)
                .build();

        mockCourse = Course.builder()
                .id(100L)
                .courseCode("CS101")
                .courseName("Lập trình Java")
                .active(true)
                .build();

        mockAssignment = Assignment.builder()
                .id(50L)
                .title("Bài tập 1")
                .course(mockCourse)
                .dueDate(LocalDateTime.now().plusDays(7)) // hạn nộp còn 7 ngày
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("submitAssignment - Nộp bài đúng hạn -> status SUBMITTED")
    void submitAssignment_beforeDueDate_returnsSubmitted() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                50L, "https://github.com/student/hw1");

        Submission savedSubmission = Submission.builder()
                .id(300L)
                .assignment(mockAssignment)
                .student(mockStudent)
                .githubUrl("https://github.com/student/hw1")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(mockAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 100L)).thenReturn(true);
        when(submissionRepository.existsByAssignmentIdAndStudentId(50L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        // Act
        SubmissionResponse response = submissionService.submitAssignment("student01", request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.getGithubUrl()).isEqualTo("https://github.com/student/hw1");
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("submitAssignment - Nộp bài trễ -> status LATE")
    void submitAssignment_afterDueDate_returnsLate() {
        // Arrange
        Assignment overdueAssignment = Assignment.builder()
                .id(51L)
                .title("Bài tập quá hạn")
                .course(mockCourse)
                .dueDate(LocalDateTime.now().minusDays(1)) // đã quá hạn
                .build();

        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                51L, "https://github.com/student/late-hw");

        Submission lateSubmission = Submission.builder()
                .id(301L)
                .assignment(overdueAssignment)
                .student(mockStudent)
                .githubUrl("https://github.com/student/late-hw")
                .status(SubmissionStatus.LATE)
                .submittedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(51L)).thenReturn(Optional.of(overdueAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 100L)).thenReturn(true);
        when(submissionRepository.existsByAssignmentIdAndStudentId(51L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(lateSubmission);

        // Act
        SubmissionResponse response = submissionService.submitAssignment("student01", request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.LATE);
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("submitAssignment - Sinh viên chưa đăng ký khóa học -> ném BusinessException")
    void submitAssignment_notEnrolled_throwsBusinessException() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                50L, "https://github.com/student/hw1");

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(mockAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 100L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitAssignment("student01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa đăng ký khóa học");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("submitAssignment - Đã nộp bài rồi -> ném DuplicateResourceException")
    void submitAssignment_alreadySubmitted_throwsDuplicateResourceException() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                50L, "https://github.com/student/hw1-v2");

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(mockAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 100L)).thenReturn(true);
        when(submissionRepository.existsByAssignmentIdAndStudentId(50L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitAssignment("student01", request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã nộp bài");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("submitAssignment - GitHub URL trống -> ném BusinessException")
    void submitAssignment_emptyGithubUrl_throwsBusinessException() {
        // Arrange
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(50L, "  ");

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(mockAssignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 100L)).thenReturn(true);
        when(submissionRepository.existsByAssignmentIdAndStudentId(50L, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitAssignment("student01", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("link GitHub");

        verify(submissionRepository, never()).save(any(Submission.class));
    }
}
