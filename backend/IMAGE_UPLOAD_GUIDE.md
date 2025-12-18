# 이미지 업로드 기능 가이드

## 구현된 기능

게시글에 이미지를 업로드하고 표시할 수 있는 기능이 추가되었습니다.

## 백엔드 변경사항

### 1. 엔티티 변경
- **Post.java**: `imageUrl` 필드 추가
- **User.java**: `posts` 컬렉션에 `@Builder.Default` 추가

### 2. DTO 변경
- **PostRequestDto**: `imageUrl` 필드 추가
- **PostResponseDto**: `imageUrl` 필드 추가

### 3. 새로운 서비스
- **FileStorageService**: 파일 저장/삭제 기능
  - `storeFile()`: 파일을 서버에 저장하고 고유한 파일명 반환
  - `deleteFile()`: 파일 삭제
  - 기본 저장 위치: `uploads/` 디렉토리

### 4. 새로운 컨트롤러
- **FileController**: 파일 업로드/다운로드 API
  - `POST /files/upload`: 파일 업로드
  - `GET /files/{fileName}`: 파일 조회
  - `DELETE /files/{fileName}`: 파일 삭제

### 5. SecurityConfig
- `/files/**` GET 요청 허용 (모든 사용자)
- `/files/upload` POST 요청 인증 필요

### 6. application.properties
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=uploads
```

## 프론트엔드 변경사항

### 1. CreatePost.jsx
- 이미지 파일 선택 input 추가
- 이미지 미리보기 기능
- 업로드 중 상태 표시

### 2. UpdatePost.jsx
- 기존 이미지 표시
- 새 이미지 업로드 기능
- 이미지 미리보기

### 3. Home.jsx
- 게시글 카드에 이미지 표시
- 이미지가 있는 경우에만 표시

### 4. CSS 스타일
- 이미지 미리보기 스타일
- 게시글 카드 이미지 스타일 (hover 효과 포함)

## 사용 방법

### 게시글 작성 시 이미지 추가
1. "Create New Post" 버튼 클릭
2. 제목과 내용 입력
3. "Image (Optional)" 필드에서 이미지 파일 선택
4. 미리보기 확인
5. "Create Post" 클릭

### 게시글 수정 시 이미지 변경
1. 게시글의 "Edit" 버튼 클릭
2. 기존 이미지가 표시됨
3. 새 이미지를 선택하면 교체됨
4. "Update Post" 클릭

### 게시글 목록에서 이미지 확인
- 이미지가 있는 게시글은 제목 아래에 이미지가 표시됨
- 카드에 마우스를 올리면 이미지가 약간 확대됨

## API 엔드포인트

### 파일 업로드
```
POST /files/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}

Request:
- file: (binary)

Response:
{
  "fileName": "uuid.jpg",
  "fileUrl": "http://localhost:8080/files/uuid.jpg",
  "fileType": "image/jpeg",
  "size": "123456"
}
```

### 파일 조회
```
GET /files/{fileName}

Response: 이미지 바이너리 데이터
```

### 게시글 생성 (이미지 포함)
```
POST /posts
Authorization: Bearer {token}

{
  "title": "제목",
  "content": "내용",
  "imageUrl": "http://localhost:8080/files/uuid.jpg"
}
```

## 주의사항

1. **파일 크기 제한**: 최대 10MB
2. **지원 형식**: 모든 이미지 형식 (image/*)
3. **저장 위치**: 프로젝트 루트의 `uploads/` 디렉토리
4. **파일명**: UUID로 자동 생성되어 중복 방지
5. **보안**: 업로드는 인증된 사용자만 가능, 조회는 모두 가능

## 향후 개선 가능 사항

1. **AWS S3 연동**: 클라우드 스토리지 사용
2. **이미지 리사이징**: 썸네일 자동 생성
3. **파일 삭제 관리**: 게시글 삭제 시 이미지 파일도 자동 삭제
4. **다중 이미지**: 한 게시글에 여러 이미지 첨부
5. **파일 타입 제한**: 특정 이미지 형식만 허용
6. **이미지 압축**: 업로드 시 자동 압축

