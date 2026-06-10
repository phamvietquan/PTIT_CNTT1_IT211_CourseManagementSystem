package com.example.coursemanagementsystem.model.dto.response;

import com.example.coursemanagementsystem.model.entity.LectureMaterial;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureMaterialResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String uploadedByName;
    private LocalDateTime createdAt;

    public static LectureMaterialResponse fromEntity(LectureMaterial material) {
        return LectureMaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .courseId(material.getCourse().getId())
                .courseCode(material.getCourse().getCourseCode())
                .courseName(material.getCourse().getCourseName())
                .fileUrl(material.getFileUrl())
                .fileName(material.getFileName())
                .fileSize(material.getFileSize())
                .uploadedByName(material.getUploadedBy().getFullName())
                .createdAt(material.getCreatedAt())
                .build();
    }
}
