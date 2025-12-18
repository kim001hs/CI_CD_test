package com.example.demo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공")
    void authenticateWithValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String userId = "testuser";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal())
                .extracting("username")
                .isEqualTo(userId);
        assertThat(authentication.isAuthenticated()).isTrue();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증하지 않음")
    void noAuthorizationHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer로 시작하지 않는 헤더는 무시")
    void authorizationHeaderWithoutBearer() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("잘못된 JWT 토큰은 인증 실패")
    void authenticateWithInvalidToken() throws ServletException, IOException {
        // given
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("빈 토큰은 인증 실패")
    void authenticateWithEmptyToken() throws ServletException, IOException {
        // given
        String authHeader = "Bearer ";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken("")).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 체인은 항상 계속 진행되어야 함")
    void filterChainAlwaysContinues() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("여러 요청에 대해 독립적으로 인증 처리")
    void authenticateMultipleRequests() throws ServletException, IOException {
        // given - 첫 번째 요청
        String token1 = "token1";
        String userId1 = "user1";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token1);
        when(jwtUtil.validateToken(token1)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token1)).thenReturn(userId1);

        // when - 첫 번째 요청 처리
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then - 첫 번째 인증 확인
        Authentication auth1 = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth1.getPrincipal())
                .extracting("username")
                .isEqualTo(userId1);

        // given - 두 번째 요청 (컨텍스트 초기화)
        SecurityContextHolder.clearContext();
        String token2 = "token2";
        String userId2 = "user2";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token2);
        when(jwtUtil.validateToken(token2)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token2)).thenReturn(userId2);

        // when - 두 번째 요청 처리
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then - 두 번째 인증 확인
        Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth2.getPrincipal())
                .extracting("username")
                .isEqualTo(userId2);
    }
}

