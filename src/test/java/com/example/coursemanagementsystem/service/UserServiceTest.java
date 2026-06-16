package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.exception.ResourceNotFoundException;
import com.example.coursemanagementsystem.model.dto.request.CreateUserRequest;
import com.example.coursemanagementsystem.model.dto.request.UpdateUserRequest;
import com.example.coursemanagementsystem.model.dto.response.UserResponse;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho UserService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("admin01")
                .password("encoded_pw")
                .email("admin@ptit.edu.vn")
                .fullName("Quản Trị Viên")
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("createUser - Request hợp lệ -> tạo user thành công")
    void createUser_validRequest_success() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .username("lecturer02")
                .password("securePass@123")
                .email("lecturer02@ptit.edu.vn")
                .fullName("Phạm Văn Giảng")
                .role(Role.LECTURER)
                .build();

        User savedUser = User.builder()
                .id(2L)
                .username("lecturer02")
                .email("lecturer02@ptit.edu.vn")
                .fullName("Phạm Văn Giảng")
                .role(Role.LECTURER)
                .active(true)
                .build();

        when(userRepository.existsByUsername("lecturer02")).thenReturn(false);
        when(userRepository.existsByEmail("lecturer02@ptit.edu.vn")).thenReturn(false);
        when(passwordEncoder.encode("securePass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("lecturer02");
        assertThat(response.getRole()).isEqualTo(Role.LECTURER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("createUser - Trùng email -> ném DuplicateResourceException")
    void createUser_duplicateEmail_throwsDuplicateResourceException() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password")
                .email("admin@ptit.edu.vn") // email đã tồn tại
                .fullName("New User")
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("admin@ptit.edu.vn")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin@ptit.edu.vn");

        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("getUserById - ID không tồn tại -> ném ResourceNotFoundException")
    void getUserById_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("updateUser - Cập nhật email trùng với user khác -> ném DuplicateResourceException")
    void updateUser_emailTakenByOtherUser_throwsDuplicateResourceException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@ptit.edu.vn"); // email của user khác

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail("taken@ptit.edu.vn")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@ptit.edu.vn");

        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("deactivateUser - ID tồn tại -> đặt active=false và lưu")
    void deactivateUser_existingId_setsActiveFalse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertThat(mockUser.getActive()).isFalse();
        verify(userRepository, times(1)).save(mockUser);
    }
}
