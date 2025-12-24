# Postman 테스트 가이드

## 사전 준비사항

1. **MySQL 데이터베이스 실행**
   - MySQL이 실행 중이어야 합니다
   - 데이터베이스 `parttime_hr`가 생성되어 있어야 합니다 (없으면 자동 생성됨)
   - `application.properties`의 DB 설정 확인:
     - URL: `jdbc:mysql://localhost:3306/parttime_hr`
     - Username: `root`
     - Password: `password`

2. **애플리케이션 실행**
   - IDE에서 `PartTimeHrApplication.java` 실행
   - 또는 터미널에서: `./mvnw spring-boot:run`
   - 기본 포트: `8080`

---

## 테스트 시나리오

### 1. 회원가입 (Signup)

**요청 정보:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/employers/signup`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "boss@example.com",
    "password": "password123",
    "passwordConfirm": "password123",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "storeName": "맛있는 카페"
  }
  ```

**예상 응답:**
- **성공 시:** `201 Created` (응답 본문 없음)
- **실패 시:** `400 Bad Request`
  ```json
  {
    "message": "이미 사용 중인 이메일입니다."
  }
  ```
  또는
  ```json
  {
    "email": "이메일 형식이 올바르지 않습니다.",
    "password": "비밀번호는 8자 이상 20자 이하여야 합니다."
  }
  ```

---

### 2. 로그인 (Login)

**요청 정보:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/employers/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "boss@example.com",
    "password": "password123"
  }
  ```

**예상 응답:**
- **성공 시:** `200 OK`
  ```
  eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib3NzQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfRU1QTE9ZRVIiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAzNjAwMH0.xxxxx
  ```
  (JWT 토큰 문자열이 그대로 반환됩니다)

- **실패 시:** `400 Bad Request`
  ```json
  {
    "message": "존재하지 않는 사용자"
  }
  ```
  또는
  ```json
  {
    "message": "비밀번호 불일치"
  }
  ```

---

### 3. JWT 토큰 검증 테스트

JWT 토큰이 제대로 발급되었는지 확인하려면:

**방법 1: JWT.io에서 확인**
1. https://jwt.io 접속
2. 로그인 응답으로 받은 토큰을 붙여넣기
3. Payload 부분에서 다음 정보 확인:
   - `sub`: 이메일 (예: "boss@example.com")
   - `role`: "ROLE_EMPLOYER"
   - `iat`: 발급 시간
   - `exp`: 만료 시간

**방법 2: 보호된 엔드포인트로 테스트**
- 현재는 보호된 엔드포인트가 없지만, 향후 추가될 엔드포인트에 다음과 같이 헤더 추가:
  ```
  Authorization: Bearer {받은_JWT_토큰}
  ```

---

## Postman Collection 예시

### Collection 생성 방법:
1. Postman에서 새 Collection 생성: "PartTimeHR API"
2. 아래 요청들을 추가:

#### Request 1: Employer Signup
```
POST http://localhost:8080/api/employers/signup
Content-Type: application/json

{
  "email": "boss@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "storeName": "맛있는 카페"
}
```

#### Request 2: Employer Login
```
POST http://localhost:8080/api/employers/login
Content-Type: application/json

{
  "email": "boss@example.com",
  "password": "password123"
}
```

#### Request 3: Login (다른 사용자)
```
POST http://localhost:8080/api/employers/login
Content-Type: application/json

{
  "email": "boss2@example.com",
  "password": "password123"
}
```

---

## 테스트 체크리스트

- [ ] 회원가입 성공 (201 Created)
- [ ] 중복 이메일로 회원가입 시도 → 실패 (400 Bad Request)
- [ ] 비밀번호 불일치로 회원가입 시도 → 실패 (400 Bad Request)
- [ ] Validation 실패 (이메일 형식 오류, 비밀번호 길이 등) → 실패 (400 Bad Request)
- [ ] 로그인 성공 → JWT 토큰 받기 (200 OK)
- [ ] 존재하지 않는 이메일로 로그인 → 실패 (400 Bad Request)
- [ ] 잘못된 비밀번호로 로그인 → 실패 (400 Bad Request)
- [ ] 받은 JWT 토큰을 jwt.io에서 확인 → Payload 정상 확인

---

## 주의사항

1. **JWT 토큰 만료 시간:** `application.properties`에서 `jwt.expiration = 3600000` (1시간)
2. **데이터베이스 초기화:** `spring.jpa.hibernate.ddl-auto=create`로 설정되어 있어서 애플리케이션 재시작 시 테이블이 삭제되고 재생성됩니다.
3. **비밀번호 암호화:** 저장되는 비밀번호는 BCrypt로 암호화되어 있습니다.

