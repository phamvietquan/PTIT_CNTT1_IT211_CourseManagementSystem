package com.example.coursemanagementsystem.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssignmentRequest {

    @NotNull(message = "Assignment ID không được để trống")
    private Long assignmentId;

    @NotBlank(message = "Link GitHub không được để trống")
    private String githubUrl;
}
