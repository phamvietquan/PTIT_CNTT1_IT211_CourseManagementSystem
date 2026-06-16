package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.CreateUserRequest;
import com.example.coursemanagementsystem.model.dto.request.UpdateUserRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.PageResponse;
import com.example.coursemanagementsystem.model.dto.response.UserResponse;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho AdminUserController (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Unit Tests")
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = UserResponse.builder()
                .id(1L)
                .username("student01")
                .email("student01@ptit.edu.vn")
                .fullName("Nguyễn Văn A")
                .role(Role.STUDENT)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("getAllUsers - Không keyword -> 200 OK với PageResponse")
    void getAllUsers_noKeyword_returns200() {
        // Arrange
        PageResponse<UserResponse> mockPage = PageResponse.<UserResponse>builder()
                .content(List.of(mockUserResponse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(userService.getAllUsers(isNull(), any(Pageable.class))).thenReturn(mockPage);

        // Act
        ResponseEntity<ApiResponse<PageResponse<UserResponse>>> response =
                adminUserController.getAllUsers(0, 10, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getTotalElements()).isEqualTo(1L);
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("getUserById - ID tồn tại -> 200 OK với UserResponse")
    void getUserById_existingId_returns200() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(mockUserResponse);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = adminUserController.getUserById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getUsername()).isEqualTo("student01");
        verify(userService, times(1)).getUserById(1L);
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("createUser - Request hợp lệ -> 201 Created với UserResponse")
    void createUser_validRequest_returns201() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .username("lecturer02")
                .password("pass@123")
                .email("lect02@ptit.edu.vn")
                .fullName("Trần Thị Giảng")
                .role(Role.LECTURER)
                .build();

        UserResponse created = UserResponse.builder()
                .id(2L)
                .username("lecturer02")
                .email("lect02@ptit.edu.vn")
                .fullName("Trần Thị Giảng")
                .role(Role.LECTURER)
                .active(true)
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(created);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response =
                adminUserController.createUser(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getRole()).isEqualTo(Role.LECTURER);
        assertThat(response.getBody().getMessage()).isEqualTo("Tạo người dùng thành công");
        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("updateUser - Request hợp lệ -> 200 OK với UserResponse đã cập nhật")
    void updateUser_validRequest_returns200() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Nguyễn Văn B Updated");

        UserResponse updated = UserResponse.builder()
                .id(1L)
                .username("student01")
                .email("student01@ptit.edu.vn")
                .fullName("Nguyễn Văn B Updated")
                .role(Role.STUDENT)
                .active(true)
                .build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updated);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response =
                adminUserController.updateUser(1L, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getFullName()).isEqualTo("Nguyễn Văn B Updated");
        assertThat(response.getBody().getMessage()).isEqualTo("Cập nhật người dùng thành công");
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("deactivateUser - ID tồn tại -> 204 No Content, gọi service.deactivate")
    void deactivateUser_existingId_returns204() {
        // Arrange
        doNothing().when(userService).deactivateUser(1L);

        // Act
        ResponseEntity<Void> response = adminUserController.deactivateUser(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService, times(1)).deactivateUser(1L);
    }
}
