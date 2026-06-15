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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Transactional
    public SubmissionResponse submitAssignment(String username, SubmitAssignmentRequest request) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment", "id", request.getAssignmentId()));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(
                student.getId(), assignment.getCourse().getId())) {
            throw new BusinessException(
                    "Bạn chưa đăng ký khóa học này, không thể nộp bài");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(
                request.getAssignmentId(), student.getId())) {
            throw new DuplicateResourceException(
                    "Bạn đã nộp bài cho assignment này rồi");
        }

        String githubUrl = request.getGithubUrl();
        if (githubUrl == null || githubUrl.isBlank()) {
            throw new BusinessException("Vui lòng cung cấp link GitHub");
        }

        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        if (assignment.getDueDate() != null
                && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            status = SubmissionStatus.LATE;
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .githubUrl(githubUrl)
                .status(status)
                .submittedAt(LocalDateTime.now())
                .build();

        Submission saved = submissionRepository.save(submission);
        return SubmissionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return submissionRepository.findByStudentId(student.getId())
                .stream()
                .map(SubmissionResponse::fromEntity)
                .collect(Collectors.toList());
    }


}
