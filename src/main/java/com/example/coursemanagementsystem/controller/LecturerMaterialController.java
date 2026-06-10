package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.LectureMaterialResponse;
import com.example.coursemanagementsystem.service.LectureMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturer/materials")
public class LecturerMaterialController {

    @Autowired
    private LectureMaterialService lectureMaterialService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> uploadMaterial(
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String lecturerUsername = authentication.getName();
        LectureMaterialResponse response = lectureMaterialService.uploadMaterial(
                lecturerUsername, courseId, title, description, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải lên tài liệu thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LectureMaterialResponse>>> getMaterialsByCourse(
            @RequestParam("courseId") Long courseId) {
        List<LectureMaterialResponse> materials = lectureMaterialService.getMaterialsByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách tài liệu thành công", materials));
    }
}
