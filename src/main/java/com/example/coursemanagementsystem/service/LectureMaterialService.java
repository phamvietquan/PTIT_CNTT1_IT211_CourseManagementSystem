package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.response.LectureMaterialResponse;
import com.example.coursemanagementsystem.model.entity.Course;
import com.example.coursemanagementsystem.model.entity.LectureMaterial;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.CourseRepository;
import com.example.coursemanagementsystem.repository.LectureMaterialRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LectureMaterialService {

    @Autowired
    private LectureMaterialRepository lectureMaterialRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;

    @Transactional
    public LectureMaterialResponse uploadMaterial(String lecturerUsername, Long courseId,
                                                   String title, String description,
                                                   MultipartFile file) {
        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", lecturerUsername));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", courseId));

        if (course.getLecturer() == null
                || !course.getLecturer().getId().equals(lecturer.getId())) {
            throw new BusinessException(
                    "Bạn không phải giảng viên của khóa học này, không thể tải lên tài liệu");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file tài liệu để tải lên");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    "Dung lượng file vượt quá giới hạn 15MB");
        }

        String savedFileName = saveFile(file, "materials");

        LectureMaterial material = LectureMaterial.builder()
                .title(title)
                .description(description)
                .course(course)
                .fileUrl("/uploads/materials/" + savedFileName)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .uploadedBy(lecturer)
                .build();

        LectureMaterial saved = lectureMaterialRepository.save(material);
        return LectureMaterialResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LectureMaterialResponse> getMaterialsByCourse(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", courseId));

        return lectureMaterialRepository.findByCourseId(courseId)
                .stream()
                .map(LectureMaterialResponse::fromEntity)
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
