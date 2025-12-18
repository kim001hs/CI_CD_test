# 🚀 로그인/회원가입 백엔드 API 문서

## 📋 완성된 기능 목록

### ✅ 1. 회원가입 (Register)
- **Endpoint**: `POST /register`
- **Request Body**:
```json
{
  "userId": "testuser",
  "password": "password123",
  "name": "홍길동"
}
```
- **Response**:
```json
{
  "id": 1,
  "userId": "testuser",
  "name": "홍길동",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
- **기능**:
  - 비밀번호 BCrypt 암호화
  - 중복 아이디 검증
  - 유효성 검사 (아이디, 비밀번호 8자 이상, 이름)
  - 회원가입 후 자동 로그인 (JWT 토큰 발급)

### ✅ 2. 로그인 (Login)
- **Endpoint**: `POST /login`
- **Request Body**:
```json
{
  "userId": "testuser",
  "password": "password123"
}
```
- **Response**:
```json
{
  "id": 1,
  "userId": "testuser",
  "name": "홍길동",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
- **기능**:
  - 비밀번호 BCrypt 검증
  - JWT 토큰 발급
  - 사용자 정보 반환

### ✅ 3. 현재 사용자 정보 조회 (인증 필요)
- **Endpoint**: `GET /me`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
```json
{
  "id": 1,
  "userId": "testuser",
  "name": "홍길동"
}
```

### ✅ 4. 현재 사용자 정보 수정 (인증 필요)
- **Endpoint**: `PUT /me`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "name": "김철수",
  "password": "newpassword123"
}
```
- **Response**:
```json
{
  "id": 1,
  "userId": "testuser",
  "name": "김철수"
}
```

### ✅ 5. 모든 사용자 조회 (인증 불필요)
- **Endpoint**: `GET /users`
- **Response**:
```json
[
  {
    "id": 1,
    "userId": "testuser",
    "name": "홍길동"
  },
  {
    "id": 2,
    "userId": "user2",
    "name": "김영희"
  }
]
```

### ✅ 6. 특정 사용자 조회 (인증 불필요)
- **Endpoint**: `GET /users/{id}`
- **Response**:
```json
{
  "id": 1,
  "userId": "testuser",
  "name": "홍길동"
}
```

---

## 🔐 JWT 인증 시스템

### JWT 토큰 사용 방법
1. **회원가입 또는 로그인** 시 JWT 토큰 받기
2. **로컬스토리지에 토큰 저장**:
   ```javascript
   localStorage.setItem('token', response.token);
   ```
3. **인증이 필요한 API 호출 시 토큰 포함**:
   ```javascript
   fetch('http://localhost:8080/me', {
     headers: {
       'Authorization': `Bearer ${localStorage.getItem('token')}`
     }
   })
   ```

### JWT 토큰 정보
- **만료 시간**: 24시간 (86400000ms)
- **알고리즘**: HS256
- **포함 정보**: userId (subject)

---

## 🛡️ 보안 기능

### ✅ 1. 비밀번호 암호화
- **BCryptPasswordEncoder** 사용
- 단방향 해시 암호화
- 레인보우 테이블 공격 방지

### ✅ 2. JWT 인증
- **Stateless** 인증 (세션 사용 안 함)
- 토큰 기반 인증
- 토큰 변조 감지

### ✅ 3. Spring Security
- CSRF 보호 (비활성화 - REST API)
- 경로별 권한 설정
- 인증 필터 체인

### ✅ 4. CORS 설정
- React 개발 서버 허용 (localhost:3000, localhost:5173)
- 허용 메서드: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Credentials 허용

### ✅ 5. 유효성 검사
- 아이디 중복 체크
- 비밀번호 최소 8자
- 필수 입력 항목 검증
- 전역 예외 처리 (`GlobalExceptionHandler`)

---

## 🗂️ 프로젝트 구조

```
src/main/java/com/example/demo/
├── auth/                      # JWT 인증 관련
│   ├── JwtUtil.java          # JWT 토큰 생성/검증
│   └── JwtAuthenticationFilter.java  # JWT 인증 필터
├── config/                    # 설정 클래스
│   ├── SecurityConfig.java   # Spring Security 설정
│   ├── PasswordConfig.java   # 비밀번호 인코더 설정
│   └── WebConfig.java        # CORS 설정
├── controller/                # REST 컨트롤러
│   ├── UserController.java   # 사용자 API
│   └── GlobalExceptionHandler.java  # 전역 예외 처리
├── domain/                    # 엔티티
│   └── User.java             # 사용자 엔티티
├── dto/                       # 데이터 전송 객체
│   ├── UserRequestDto.java   # 요청 DTO
│   └── UserResponseDto.java  # 응답 DTO
├── repository/                # 데이터 액세스
│   └── UserRepository.java   # 사용자 Repository
└── service/                   # 비즈니스 로직
    └── UserService.java      # 사용자 서비스
```

---

## 🧪 테스트

### 작성된 테스트 (총 25개)
- ✅ **DemoApplicationTests**: 통합 테스트 (15개)
- ✅ **JwtUtilTest**: JWT 유틸 단위 테스트 (11개)
- ✅ **JwtAuthenticationFilterTest**: JWT 필터 단위 테스트 (8개)
- ✅ **UserControllerTest**: 컨트롤러 테스트

### 테스트 실행
```bash
./gradlew test
```

---

## 🔧 설정 파일

### application.properties
```properties
# JWT 설정
jwt.secret=demo-jwt-secret-key-please-change-this-to-strong-random-key-in-production-at-least-256-bits-long
jwt.expiration=86400000

# H2 데이터베이스
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

---

## 📝 의존성

### build.gradle
```groovy
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // H2 Database
    runtimeOnly 'com.h2database:h2'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## 🚀 서버 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 DemoApplication 실행
```

**서버 주소**: http://localhost:8080

---

## 📱 프론트엔드 연동 예시

### React에서 사용하는 예시

```javascript
// 회원가입
const register = async (userId, password, name) => {
  const response = await fetch('http://localhost:8080/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, password, name })
  });
  const data = await response.json();
  localStorage.setItem('token', data.token); // 토큰 저장
  return data;
};

// 로그인
const login = async (userId, password) => {
  const response = await fetch('http://localhost:8080/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, password })
  });
  const data = await response.json();
  localStorage.setItem('token', data.token); // 토큰 저장
  return data;
};

// 인증이 필요한 API 호출
const getMyInfo = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};

// 로그아웃
const logout = () => {
  localStorage.removeItem('token');
};
```

---

## ⚠️ 주의사항

### Production 배포 시 반드시 변경해야 할 것들:

1. **JWT Secret Key**
   - 환경 변수로 관리
   - 강력한 랜덤 키 사용 (최소 256비트)
   ```bash
   export JWT_SECRET="your-production-secret-key"
   ```

2. **CORS 설정**
   - 실제 프론트엔드 도메인으로 변경
   ```java
   .allowedOrigins("https://your-production-domain.com")
   ```

3. **데이터베이스**
   - H2 → MySQL/PostgreSQL 등으로 변경
   - 데이터베이스 연결 정보 환경 변수로 관리

4. **HTTPS 사용**
   - SSL 인증서 적용
   - Secure 쿠키 설정

5. **로깅**
   - SQL 로깅 비활성화 (`spring.jpa.show-sql=false`)
   - 프로덕션 레벨 로그 설정

---

## ✅ 완성된 기능 체크리스트

- ✅ 회원가입 (비밀번호 암호화, 유효성 검사, 자동 로그인)
- ✅ 로그인 (JWT 토큰 발급)
- ✅ JWT 인증 시스템
- ✅ 사용자 정보 조회/수정
- ✅ Spring Security 설정
- ✅ CORS 설정
- ✅ 전역 예외 처리
- ✅ 통합/단위 테스트 (25개)
- ✅ H2 데이터베이스 연동
- ✅ RESTful API 설계

---

## 🎉 완성!

**백엔드 로그인/회원가입 시스템이 완벽하게 구현되었습니다!**

이제 프론트엔드(React)에서 이 API를 호출하여 사용할 수 있습니다.

