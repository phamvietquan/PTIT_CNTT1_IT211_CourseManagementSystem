package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.GradeSubmissionRequest;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.Submission;
import com.example.coursemanagementsystem.model.entity.SubmissionStatus;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.SubmissionRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradingService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public SubmissionResponse gradeSubmission(String lecturerUsername,
                                              GradeSubmissionRequest request) {
        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", lecturerUsername));

        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission", "id", request.getSubmissionId()));

        if (submission.getAssignment().getCourse().getLecturer() == null
                || !submission.getAssignment().getCourse().getLecturer().getId()
                        .equals(lecturer.getId())) {
            throw new BusinessException(
                    "Bạn không phải giảng viên của khóa học này, không thể chấm điểm");
        }

        if (submission.getStatus() == SubmissionStatus.PENDING) {
            throw new BusinessException(
                    "Sinh viên chưa nộp bài, không thể chấm điểm");
        }

        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BusinessException(
                    "Bài nộp này đã được chấm điểm rồi");
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        return SubmissionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsForAssignment(String lecturerUsername,
                                                                Long assignmentId) {
        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", lecturerUsername));

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        if (!submissions.isEmpty()) {
            Submission first = submissions.get(0);
            if (first.getAssignment().getCourse().getLecturer() == null
                    || !first.getAssignment().getCourse().getLecturer().getId()
                            .equals(lecturer.getId())) {
                throw new BusinessException(
                        "Bạn không phải giảng viên của khóa học này");
            }
        }

        return submissions.stream()
                .map(SubmissionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
