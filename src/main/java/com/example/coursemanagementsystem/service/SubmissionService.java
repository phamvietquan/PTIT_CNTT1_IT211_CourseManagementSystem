package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.response.SubmissionResponse;
import com.example.coursemanagementsystem.model.entity.*;
import com.example.coursemanagementsystem.repository.AssignmentRepository;
import com.example.coursemanagementsystem.repository.EnrollmentRepository;
import com.example.coursemanagementsystem.repository.SubmissionRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional
    public SubmissionResponse submitAssignment(String username, Long assignmentId,
                                               String githubUrl, MultipartFile file) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment", "id", assignmentId));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(
                student.getId(), assignment.getCourse().getId())) {
            throw new BusinessException(
                    "Bạn chưa đăng ký khóa học này, không thể nộp bài");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(
                assignmentId, student.getId())) {
            throw new DuplicateResourceException(
                    "Bạn đã nộp bài cho assignment này rồi");
        }

        if ((githubUrl == null || githubUrl.isBlank()) && (file == null || file.isEmpty())) {
            throw new BusinessException(
                    "Vui lòng cung cấp link GitHub hoặc tải lên file bài nộp");
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

        if (file != null && !file.isEmpty()) {
            String savedFileName = saveFile(file, "submissions");
            submission.setFileUrl("/uploads/submissions/" + savedFileName);
            submission.setFileName(file.getOriginalFilename());
        }

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

    private String saveFile(MultipartFile file, String subDir) {
        try {
            Path uploadPath = Paths.get(uploadDir, subDir);
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException e) {
            throw new BusinessException("Không thể lưu file: " + e.getMessage());
        }
    }
}
