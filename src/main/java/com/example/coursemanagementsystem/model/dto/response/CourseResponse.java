package com.example.coursemanagementsystem.model.dto.response;

import com.example.coursemanagementsystem.model.entity.Course;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer maxStudents;
    private Long lecturerId;
    private String lecturerName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .maxStudents(course.getMaxStudents())
                .lecturerId(course.getLecturer() != null ? course.getLecturer().getId() : null)
                .lecturerName(course.getLecturer() != null ? course.getLecturer().getFullName() : null)
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
