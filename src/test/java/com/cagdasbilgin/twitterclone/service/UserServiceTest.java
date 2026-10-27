package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.AuthResponse;
import com.cagdasbilgin.twitterclone.dto.LoginRequest;
import com.cagdasbilgin.twitterclone.dto.RegisterRequest;
import com.cagdasbilgin.twitterclone.dto.UserResponse;
import com.cagdasbilgin.twitterclone.entity.User;
import com.cagdasbilgin.twitterclone.exception.TwitterException;
import com.cagdasbilgin.twitterclone.repository.UserRepository;
import com.cagdasbilgin.twitterclone.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_pass")
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_WhenValid_ShouldSaveUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("rawPassword123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("new@example.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("another@example.com")
                .password("pass")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        TwitterException ex = assertThrows(TwitterException.class, () -> userService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .username("unique_user")
                .email("test@example.com")
                .password("pass")
                .build();

        when(userRepository.existsByUsername("unique_user")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        TwitterException ex = assertThrows(TwitterException.class, () -> userService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void login_WhenCredentialsMatch_ShouldReturnAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("correct_pass")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_pass", "encoded_pass")).thenReturn(true);

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void login_WhenPasswordWrong_ShouldThrowUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrong_pass")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);

        TwitterException ex = assertThrows(TwitterException.class, () -> userService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .username("ghost")
                .password("pass")
                .build();

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        TwitterException ex = assertThrows(TwitterException.class, () -> userService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
