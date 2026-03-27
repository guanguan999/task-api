package com.guanyiping.task.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanyiping.task.management.dto.AuthRequest;
import com.guanyiping.task.management.dto.AuthResponse;
import com.guanyiping.task.management.exception.DuplicateResourceException;
import com.guanyiping.task.management.security.JwtUtil;
import com.guanyiping.task.management.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // JwtFilter が依存するビーンをモック化
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- /auth/register ---

    @Test
    void register_success_returns201AndToken() throws Exception {
        AuthRequest request = buildRequest("test@example.com", "testuser", "password123");
        when(authService.register(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt_token"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        AuthRequest request = buildRequest("not-an-email", "testuser", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void register_blankEmail_returns400() throws Exception {
        AuthRequest request = buildRequest("", "testuser", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        AuthRequest request = buildRequest("test@example.com", "testuser", "abc");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        AuthRequest request = buildRequest("test@example.com", "testuser", "password123");
        when(authService.register(any(AuthRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already registered: test@example.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already registered: test@example.com"));
    }

    // --- /auth/login ---

    @Test
    void login_success_returns200AndToken() throws Exception {
        AuthRequest request = buildRequest("test@example.com", "testuser", "password123");
        when(authService.login(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt_token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        AuthRequest request = buildRequest("test@example.com", "testuser", "wrongpassword");
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_missingFields_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    private AuthRequest buildRequest(String email, String username, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}