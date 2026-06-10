package com.example.coursemanagementsystem.model.dto.response;

import com.example.coursemanagementsystem.model.entity.Submission;
import com.example.coursemanagementsystem.model.entity.SubmissionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String githubUrl;
    private String fileUrl;
    private String fileName;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;

    public static SubmissionResponse fromEntity(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getFullName())
                .githubUrl(submission.getGithubUrl())
                .fileUrl(submission.getFileUrl())
                .fileName(submission.getFileName())
                .status(submission.getStatus())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}
