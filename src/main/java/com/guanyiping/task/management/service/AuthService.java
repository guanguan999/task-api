package com.guanyiping.task.management.service;

import com.guanyiping.task.management.dto.AuthRequest;
import com.guanyiping.task.management.dto.AuthResponse;
import com.guanyiping.task.management.entity.User;
import com.guanyiping.task.management.repository.UserRepository;
import com.guanyiping.task.management.security.CustomUserDetails;
import com.guanyiping.task.management.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(new CustomUserDetails(user)));
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new AuthResponse(jwtUtil.generateToken(new CustomUserDetails(user)));
    }
}
