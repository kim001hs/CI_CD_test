package com.example.demo.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private final String testSecretKey = "test-secret-key-minimum-256-bits-long-for-HS256-algorithm-security";
    private final Long testExpiration = 3600000L; // 1시간

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공")
    void generateToken() {
        // given
        String userId = "testuser";

        // when
        String token = jwtUtil.generateToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 형식
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateValidToken() {
        // given
        String userId = "testuser";
        String token = jwtUtil.generateToken(userId);

        // when
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateInvalidFormatToken() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateEmptyToken() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtUtil.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void validateNullToken() {
        // given
        String nullToken = null;

        // when
        boolean isValid = jwtUtil.validateToken(nullToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 성공")
    void getUserIdFromToken() {
        // given
        String userId = "testuser123";
        String token = jwtUtil.generateToken(userId);

        // when
        String extractedUserId = jwtUtil.getUserIdFromToken(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("여러 사용자 ID로 토큰 생성 및 검증")
    void generateTokensForMultipleUsers() {
        // given
        String[] userIds = {"user1", "user2", "user3", "admin", "test@example.com"};

        for (String userId : userIds) {
            // when
            String token = jwtUtil.generateToken(userId);
            String extractedUserId = jwtUtil.getUserIdFromToken(token);

            // then
            assertThat(extractedUserId).isEqualTo(userId);
            assertThat(jwtUtil.validateToken(token)).isTrue();
        }
    }

    @Test
    @DisplayName("생성된 토큰이 올바른 만료 시간을 가지는지 확인")
    void tokenHasCorrectExpiration() {
        // given
        String userId = "testuser";

        // when
        String token = jwtUtil.generateToken(userId);

        // then - 토큰이 현재 유효해야 함
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("서로 다른 사용자의 토큰은 다른 값을 가짐")
    void differentUsersHaveDifferentTokens() {
        // given
        String userId1 = "user1";
        String userId2 = "user2";

        // when
        String token1 = jwtUtil.generateToken(userId1);
        String token2 = jwtUtil.generateToken(userId2);

        // then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.getUserIdFromToken(token1)).isEqualTo(userId1);
        assertThat(jwtUtil.getUserIdFromToken(token2)).isEqualTo(userId2);
    }

    @Test
    @DisplayName("동일한 사용자의 토큰도 생성 시점에 따라 다름")
    void sameUserDifferentTimeHasDifferentTokens() throws InterruptedException {
        // given
        String userId = "testuser";

        // when
        String token1 = jwtUtil.generateToken(userId);
        Thread.sleep(1000); // 1초 대기
        String token2 = jwtUtil.generateToken(userId);

        // then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.getUserIdFromToken(token1)).isEqualTo(userId);
        assertThat(jwtUtil.getUserIdFromToken(token2)).isEqualTo(userId);
    }

    @Test
    @DisplayName("변조된 토큰 검증 실패")
    void validateTamperedToken() {
        // given
        String userId = "testuser";
        String token = jwtUtil.generateToken(userId);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // when
        boolean isValid = jwtUtil.validateToken(tamperedToken);

        // then
        assertThat(isValid).isFalse();
    }
}

