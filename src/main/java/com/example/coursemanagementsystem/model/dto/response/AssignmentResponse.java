package com.example.coursemanagementsystem.model.dto.response;

import com.example.coursemanagementsystem.model.entity.Assignment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;

    public static AssignmentResponse fromEntity(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourse().getId())
                .courseCode(assignment.getCourse().getCourseCode())
                .courseName(assignment.getCourse().getCourseName())
                .dueDate(assignment.getDueDate())
                .createdAt(assignment.getCreatedAt())
                .build();
    }
}
