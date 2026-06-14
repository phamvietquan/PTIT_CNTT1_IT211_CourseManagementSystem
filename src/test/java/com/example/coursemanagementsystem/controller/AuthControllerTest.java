package com.example.coursemanagementsystem.controller;

import com.example.coursemanagementsystem.model.dto.request.LoginRequest;
import com.example.coursemanagementsystem.model.dto.request.RegisterRequest;
import com.example.coursemanagementsystem.model.dto.response.ApiResponse;
import com.example.coursemanagementsystem.model.dto.response.AuthResponse;
import com.example.coursemanagementsystem.model.dto.response.UserResponse;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AF2 - Unit Tests cho AuthController (5 tests) - dùng Mockito thuần, không cần Spring context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserResponse mockUserResponse;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.STUDENT)
                .active(true)
                .build();

        mockAuthResponse = AuthResponse.builder()
                .accessToken("mock.access.token")
                .refreshToken("mock.refresh.token")
                .userId(1L)
                .username("testuser")
                .role("STUDENT")
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("register - Request hợp lệ -> 201 Created với UserResponse")
    void register_validRequest_returns201WithUserResponse() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockUserResponse);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = authController.register(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getMessage()).isEqualTo("Đăng ký tài khoản thành công");
        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("login - Credentials hợp lệ -> 200 OK với AuthResponse")
    void login_validCredentials_returns200WithAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getMessage()).isEqualTo("Đăng nhập thành công");
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("logout - Có Bearer token hợp lệ -> 200 OK và gọi service.logout")
    void logout_withBearerToken_callsServiceAndReturns200() {
        // Arrange
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        doNothing().when(authService).logout("valid.jwt.token");

        // Act
        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Đăng xuất thành công");
        verify(authService, times(1)).logout("valid.jwt.token");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("logout - Không có Authorization header -> 200 OK, không gọi service.logout")
    void logout_withoutBearerToken_returns200WithoutCallingService() {
        // Arrange
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("Authorization")).thenReturn(null);

        // Act
        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        // Không có token -> không gọi logout service
        verify(authService, never()).logout(anyString());
    }

}
