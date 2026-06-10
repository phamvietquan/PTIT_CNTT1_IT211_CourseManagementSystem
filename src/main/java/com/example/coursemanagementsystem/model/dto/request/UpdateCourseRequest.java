package com.example.coursemanagementsystem.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {

    @Size(max = 200, message = "Tên khóa học tối đa 200 ký tự")
    private String courseName;

    private String description;

    private Integer maxStudents;

    private Long lecturerId;

    private Boolean active;
}
