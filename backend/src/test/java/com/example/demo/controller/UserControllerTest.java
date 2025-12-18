package com.example.demo.controller;

import com.example.demo.dto.UserRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Cascade로 Post와 Comment도 자동 삭제
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll(); // 테스트 후 데이터 정리
    }

    private String toJson(String userId, String password, String name) {
        StringBuilder sb = new StringBuilder("{");
        if (userId != null) sb.append("\"userId\":\"").append(userId).append("\"");
        if (password != null) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"password\":\"").append(password).append("\"");
        }
        if (name != null) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"name\":\"").append(name).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    @Test
    @DisplayName("POST /api/register - 회원가입 성공")
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "password123", "테스트유저")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/register - 중복 아이디 회원가입 실패")
    void registerDuplicateUserId() throws Exception {
        userService.register(UserRequestDto.builder()
                .userId("testuser")
                .password("password123")
                .name("유저1")
                .build());

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "password456", "유저2")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/login - 로그인 성공")
    void loginSuccess() throws Exception {
        userService.register(UserRequestDto.builder()
                .userId("testuser")
                .password("password123")
                .name("테스트유저")
                .build());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "password123", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("POST /api/login - 존재하지 않는 아이디로 로그인 실패")
    void loginWithNonExistentUserId() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("nonexistent", "password123", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/login - 잘못된 비밀번호로 로그인 실패")
    void loginWithWrongPassword() throws Exception {
        userService.register(UserRequestDto.builder()
                .userId("testuser")
                .password("password123")
                .name("테스트유저")
                .build());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "wrongpassword", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users - 모든 사용자 조회")
    void getAllUsers() throws Exception {
        userService.register(UserRequestDto.builder()
                .userId("user1")
                .password("password123")
                .name("유저1")
                .build());
        userService.register(UserRequestDto.builder()
                .userId("user2")
                .password("password123")
                .name("유저2")
                .build());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].userId", containsInAnyOrder("user1", "user2")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - ID로 사용자 조회 성공")
    void getUserById() throws Exception {
        Long userId = userService.register(UserRequestDto.builder()
                .userId("testuser")
                .password("password123")
                .name("테스트유저")
                .build());
        UserResponseDto registered = userService.findById(userId);

        mockMvc.perform(get("/api/users/{id}", registered.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registered.getId()))
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트유저"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 존재하지 않는 ID로 조회 실패")
    void getUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 → 로그인 → 조회 통합 시나리오")
    void fullUserFlowScenario() throws Exception {
        // 1. 회원가입
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "password123", "테스트유저")))
                .andExpect(status().isOk());

        UserResponseDto registeredUser = userService.findByUserId("testuser");

        // 2. 로그인
        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson("testuser", "password123", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testuser"));

        // 3. 사용자 조회
        mockMvc.perform(get("/api/users/{id}", registeredUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트유저"));

        // 4. 전체 사용자 목록 조회
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("testuser"));
    }
}

