package com.guanyiping.task.management.service;

import com.guanyiping.task.management.dto.AuthRequest;
import com.guanyiping.task.management.dto.AuthResponse;
import com.guanyiping.task.management.entity.User;
import com.guanyiping.task.management.exception.DuplicateResourceException;
import com.guanyiping.task.management.exception.ResourceNotFoundException;
import com.guanyiping.task.management.repository.UserRepository;
import com.guanyiping.task.management.security.CustomUserDetails;
import com.guanyiping.task.management.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private AuthRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encoded_password");
    }

    // ---- register ----

    @Test
    void register_success_returnsToken() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(CustomUserDetails.class))).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt_token");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_encodesPassword_beforeSaving() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            assertThat(saved.getPassword()).isEqualTo("encoded_password");
            return saved;
        });
        when(jwtUtil.generateToken(any(CustomUserDetails.class))).thenReturn("jwt_token");

        authService.register(request);
    }

    // ---- login ----

    @Test
    void login_success_returnsToken() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(CustomUserDetails.class))).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt_token");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("test@example.com", "password123")
        );
    }

    @Test
    void login_throwsBadCredentialsException_whenPasswordWrong() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void login_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
