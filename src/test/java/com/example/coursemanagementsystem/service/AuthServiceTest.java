package com.example.coursemanagementsystem.service;

import com.example.coursemanagementsystem.exception.BusinessException;
import com.example.coursemanagementsystem.exception.DuplicateResourceException;
import com.example.coursemanagementsystem.model.dto.request.ChangePasswordRequest;
import com.example.coursemanagementsystem.model.dto.request.LoginRequest;
import com.example.coursemanagementsystem.model.dto.request.RegisterRequest;
import com.example.coursemanagementsystem.model.dto.response.AuthResponse;
import com.example.coursemanagementsystem.model.dto.response.UserResponse;
import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.model.entity.TokenBlacklist;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.TokenBlacklistRepository;
import com.example.coursemanagementsystem.repository.UserRepository;
import com.example.coursemanagementsystem.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AF2 - Unit Tests cho AuthService (5 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.STUDENT)
                .active(true)
                .build();
    }

    // ===================== TEST 1 =====================
    @Test
    @DisplayName("register - Đăng ký user mới thành công")
    void register_validRequest_success() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .email("newuser@example.com")
                .fullName("New User")
                .build();

        User savedUser = User.builder()
                .id(2L)
                .username("newuser")
                .password("encoded")
                .email("newuser@example.com")
                .fullName("New User")
                .role(Role.STUDENT)
                .active(true)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ===================== TEST 2 =====================
    @Test
    @DisplayName("register - Trùng username -> ném DuplicateResourceException")
    void register_duplicateUsername_throwsDuplicateResourceException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("another@example.com")
                .fullName("Another User")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== TEST 3 =====================
    @Test
    @DisplayName("login - Tài khoản bị khóa (inactive) -> ném BusinessException")
    void login_inactiveUser_throwsBusinessException() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");

        User inactiveUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.STUDENT)
                .active(false)
                .build();

        Authentication mockAuth = mock(Authentication.class);
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Inactive");
    }

    // ===================== TEST 4 =====================
    @Test
    @DisplayName("logout - Token hợp lệ -> thêm vào blacklist thành công")
    void logout_validToken_addsToBlacklist() {
        // Arrange
        String token = "valid.jwt.token";
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour later

        when(jwtProvider.getExpirationDateFromToken(token)).thenReturn(futureDate);
        when(tokenBlacklistRepository.save(any(TokenBlacklist.class)))
                .thenReturn(mock(TokenBlacklist.class));

        // Act
        authService.logout(token);

        // Assert
        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo(token);
    }

    // ===================== TEST 5 =====================
    @Test
    @DisplayName("changePassword - Sai mật khẩu cũ -> ném BadCredentialsException")
    void changePassword_wrongOldPassword_throwsBadCredentialsException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPass", "newPass123");
        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOldPass", "encoded_password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(username, request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Mật khẩu cũ không chính xác");

        verify(userRepository, never()).save(any(User.class));
    }
}
