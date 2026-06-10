package com.example.coursemanagementsystem.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Mã khóa học không được để trống")
    @Size(max = 20, message = "Mã khóa học tối đa 20 ký tự")
    private String courseCode;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 200, message = "Tên khóa học tối đa 200 ký tự")
    private String courseName;

    private String description;

    private Integer maxStudents;

    @NotNull(message = "ID giảng viên không được để trống")
    private Long lecturerId;
}
