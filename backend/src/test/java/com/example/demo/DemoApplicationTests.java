package com.example.demo;

import com.example.demo.auth.JwtUtil;
import com.example.demo.domain.User;
import com.example.demo.dto.UserRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class DemoApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("컨텍스트 로드 테스트")
	void contextLoads() {
		assertThat(userService).isNotNull();
		assertThat(userRepository).isNotNull();
		assertThat(passwordEncoder).isNotNull();
	}

	@Test
	@DisplayName("회원가입 성공")
	void registerSuccess() {
		// given
		UserRequestDto request = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();

		// when
		Long userId = userService.register(request);
		UserResponseDto response = userService.findById(userId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo("testuser");
		assertThat(response.getName()).isEqualTo("테스트유저");
		assertThat(response.getId()).isNotNull();
	}

	@Test
	@DisplayName("중복 아이디 회원가입 실패")
	void registerDuplicateUserId() {
		// given
		UserRequestDto request1 = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("유저1")
				.build();
		userService.register(request1);

		UserRequestDto request2 = UserRequestDto.builder()
				.userId("testuser")
				.password("password456")
				.name("유저2")
				.build();

		// when & then
		assertThatThrownBy(() -> userService.register(request2))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("이미 존재하는 사용자입니다.");
	}

	@Test
	@DisplayName("아이디 없이 회원가입 실패")
	void registerWithoutUserId() {
		// given
		UserRequestDto request = UserRequestDto.builder()
				.userId(null)
				.password("password123")
				.name("테스트유저")
				.build();

		// when & then
		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("사용자 ID는 필수 입력 항목입니다.");
	}

	@Test
	@DisplayName("짧은 비밀번호로 회원가입 실패")
	void registerWithShortPassword() {
		// given
		UserRequestDto request = UserRequestDto.builder()
				.userId("testuser")
				.password("short")
				.name("테스트유저")
				.build();

		// when & then
		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("비밀번호는 8자 이상이어야 합니다.");
	}

	@Test
	@DisplayName("이름 없이 회원가입 실패")
	void registerWithoutName() {
		// given
		UserRequestDto request = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name(null)
				.build();

		// when & then
		assertThatThrownBy(() -> userService.register(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("이름은 필수 입력 항목입니다.");
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() {
		// given
		UserRequestDto registerRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();
		userService.register(registerRequest);

		UserRequestDto loginRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.build();

		// when
		UserResponseDto response = userService.login(loginRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo("testuser");
		assertThat(response.getName()).isEqualTo("테스트유저");
		assertThat(response.getToken()).isNotNull();
		assertThat(response.getToken()).isNotEmpty();
	}

	@Test
	@DisplayName("존재하지 않는 아이디로 로그인 실패")
	void loginWithNonExistentUserId() {
		// given
		UserRequestDto loginRequest = UserRequestDto.builder()
				.userId("nonexistent")
				.password("password123")
				.build();

		// when & then
		assertThatThrownBy(() -> userService.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("존재하지 않는 사용자입니다.");
	}

	@Test
	@DisplayName("잘못된 비밀번호로 로그인 실패")
	void loginWithWrongPassword() {
		// given
		UserRequestDto registerRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();
		userService.register(registerRequest);

		UserRequestDto loginRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("wrongpassword")
				.build();

		// when & then
		assertThatThrownBy(() -> userService.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("비밀번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("ID로 사용자 조회 성공")
	void findByIdSuccess() {
		// given
		UserRequestDto registerRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();
		Long userId = userService.register(registerRequest);

		// when
		UserResponseDto registered = userService.findById(userId);
		UserResponseDto found = userService.findById(registered.getId());

		// then
		assertThat(found).isNotNull();
		assertThat(found.getId()).isEqualTo(registered.getId());
		assertThat(found.getUserId()).isEqualTo("testuser");
		assertThat(found.getName()).isEqualTo("테스트유저");
	}

	@Test
	@DisplayName("존재하지 않는 ID로 조회 실패")
	void findByIdNotFound() {
		// when & then
		assertThatThrownBy(() -> userService.findById(999L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("사용자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("userId로 사용자 조회 성공")
	void findByUserIdSuccess() {
		// given
		UserRequestDto registerRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();
		userService.register(registerRequest);

		// when
		UserResponseDto found = userService.findByUserId("testuser");

		// then
		assertThat(found).isNotNull();
		assertThat(found.getUserId()).isEqualTo("testuser");
		assertThat(found.getName()).isEqualTo("테스트유저");
	}

	@Test
	@DisplayName("모든 사용자 조회 성공")
	void findAllSuccess() {
		// given
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
		userService.register(UserRequestDto.builder()
				.userId("user3")
				.password("password123")
				.name("유저3")
				.build());

		// when
		List<UserResponseDto> users = userService.findAll();

		// then
		assertThat(users).hasSize(3);
		assertThat(users).extracting("userId")
				.containsExactlyInAnyOrder("user1", "user2", "user3");
	}

	@Test
	@DisplayName("비밀번호가 암호화되어 저장되는지 확인")
	void passwordEncryption() {
		// given
		String rawPassword = "password123";
		UserRequestDto request = UserRequestDto.builder()
				.userId("testuser")
				.password(rawPassword)
				.name("테스트유저")
				.build();

		// when
		Long userId=userService.register(request);
		UserResponseDto response = userService.findById(userId);
		User savedUser = userRepository.findById(response.getId()).orElseThrow();

		// then
		assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
		assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();
	}

	@Test
	@DisplayName("JWT 토큰 생성 및 검증 성공")
	void jwtTokenGenerationAndValidation() {
		// given
		String userId = "testuser";

		// when
		String token = jwtUtil.generateToken(userId);

		// then
		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();
		assertThat(jwtUtil.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("JWT 토큰에서 사용자 ID 추출 성공")
	void extractUserIdFromToken() {
		// given
		String userId = "testuser";
		String token = jwtUtil.generateToken(userId);

		// when
		String extractedUserId = jwtUtil.getUserIdFromToken(token);

		// then
		assertThat(extractedUserId).isEqualTo(userId);
	}

	@Test
	@DisplayName("잘못된 JWT 토큰 검증 실패")
	void invalidTokenValidation() {
		// given
		String invalidToken = "invalid.jwt.token";

		// when
		boolean isValid = jwtUtil.validateToken(invalidToken);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("로그인 시 JWT 토큰이 반환되고 유효한지 확인")
	void loginReturnsValidJwtToken() {
		// given
		UserRequestDto registerRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.name("테스트유저")
				.build();
		userService.register(registerRequest);

		UserRequestDto loginRequest = UserRequestDto.builder()
				.userId("testuser")
				.password("password123")
				.build();

		// when
		UserResponseDto response = userService.login(loginRequest);

		// then
		assertThat(response.getToken()).isNotNull();
		assertThat(jwtUtil.validateToken(response.getToken())).isTrue();
		assertThat(jwtUtil.getUserIdFromToken(response.getToken())).isEqualTo("testuser");
	}
}
