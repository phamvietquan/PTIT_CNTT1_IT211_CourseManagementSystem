package com.example.coursemanagementsystem.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollCourseRequest {

    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;
}
