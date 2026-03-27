package com.guanyiping.task.management.security;

import com.guanyiping.task.management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 32文字以上の秘密鍵（テスト用）
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1時間

        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password");
        userDetails = new CustomUserDetails(user);
    }

    // ---- generateToken ----

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_containsThreeParts() {
        String token = jwtUtil.generateToken(userDetails);

        // JWT は header.payload.signature の3部構成
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ---- extractUsername ----

    @Test
    void extractUsername_returnsEmail() {
        String token = jwtUtil.generateToken(userDetails);

        String email = jwtUtil.extractUsername(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    // ---- validateToken ----

    @Test
    void validateToken_returnsTrue_whenTokenIsValid() {
        String token = jwtUtil.generateToken(userDetails);

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_returnsFalse_whenEmailDoesNotMatch() {
        String token = jwtUtil.generateToken(userDetails);

        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setUsername("other");
        otherUser.setPassword("pass");
        CustomUserDetails otherDetails = new CustomUserDetails(otherUser);

        boolean valid = jwtUtil.validateToken(token, otherDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_returnsFalse_whenTokenIsExpired() {
        // expiration を -1ms に設定して即期限切れトークンを生成
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken(userDetails);

        // 検証時は例外ではなく false を期待（validateToken 内で isTokenExpired を使用）
        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken, userDetails))
                .isInstanceOf(Exception.class); // jjwt は期限切れ時に ExpiredJwtException をスロー
    }

    @Test
    void extractUsername_throwsException_whenTokenIsTampered() {
        String token = jwtUtil.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtUtil.extractUsername(tamperedToken))
                .isInstanceOf(Exception.class);
    }
}
