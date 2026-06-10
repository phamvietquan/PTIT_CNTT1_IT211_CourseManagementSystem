package com.example.coursemanagementsystem.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmissionRequest {

    @NotNull(message = "Submission ID không được để trống")
    private Long submissionId;

    @NotNull(message = "Điểm số không được để trống")
    @Min(value = 0, message = "Điểm số phải từ 0")
    @Max(value = 100, message = "Điểm số tối đa là 100")
    private Integer score;

    private String feedback;
}
