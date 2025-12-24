# Postman 테스트 시나리오

## 🎯 현재 구현된 기능

1. ✅ Employer 회원가입
2. ✅ Employer 로그인 (JWT 토큰 발급)
3. ✅ 현재 로그인한 사용자 정보 조회
4. ✅ 프로필 조회
5. ✅ 사장님 전용 대시보드 (인가 테스트)

---

## 📋 테스트 시나리오

### 1️⃣ 회원가입 테스트

**요청:**
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

**예상 응답:** `201 Created` (응답 본문 없음)

**추가 테스트:**
- ❌ 중복 이메일로 회원가입 시도 → `400 Bad Request` + `{"message": "이미 사용 중인 이메일입니다."}`
- ❌ 비밀번호 불일치 → `400 Bad Request` + `{"message": "비밀번호가 일치하지 않습니다."}`
- ❌ Validation 실패 (이메일 형식 오류, 비밀번호 길이 등) → `400 Bad Request` + 필드별 에러

---

### 2️⃣ 로그인 테스트 (JWT 토큰 받기)

**요청:**
```
POST http://localhost:8080/api/employers/login
Content-Type: application/json

{
  "email": "boss@example.com",
  "password": "password123"
}
```

**예상 응답:** `200 OK`
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib3NzQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfRU1QTE9ZRVIiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAzNjAwMH0.xxxxx
```

**추가 테스트:**
- ❌ 존재하지 않는 이메일 → `400 Bad Request` + `{"message": "존재하지 않는 사용자"}`
- ❌ 잘못된 비밀번호 → `400 Bad Request` + `{"message": "비밀번호 불일치"}`

**JWT 토큰 확인:**
- 받은 토큰을 https://jwt.io 에서 확인
- Payload에 `sub` (이메일), `role` (ROLE_EMPLOYER) 확인

---

### 3️⃣ 현재 사용자 정보 조회 (인증 테스트)

**요청:**
```
GET http://localhost:8080/api/employers/me
Authorization: Bearer {위에서 받은 JWT 토큰}
```

**예상 응답:** `200 OK`
```json
{
  "id": 1,
  "email": "boss@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "storeName": "맛있는 카페",
  "role": "ROLE_EMPLOYER",
  "createdAt": "2025-12-24T22:30:00"
}
```

**추가 테스트:**
- ❌ 토큰 없이 요청 → `401 Unauthorized`
- ❌ 잘못된 토큰 → `401 Unauthorized`
- ❌ 만료된 토큰 → `401 Unauthorized`

---

### 4️⃣ 프로필 조회 (인증 테스트)

**요청:**
```
GET http://localhost:8080/api/employers/profile
Authorization: Bearer {JWT 토큰}
```

**예상 응답:** `200 OK`
```json
{
  "message": "프로필 페이지입니다.",
  "email": "boss@example.com",
  "authorities": "[ROLE_EMPLOYER]"
}
```

**테스트:**
- ✅ 인증된 사용자는 모두 접근 가능
- ❌ 토큰 없이 요청 → `401 Unauthorized`

---

### 5️⃣ 사장님 대시보드 (인가 테스트)

**요청:**
```
GET http://localhost:8080/api/employers/dashboard
Authorization: Bearer {JWT 토큰}
```

**예상 응답:** `200 OK`
```json
{
  "message": "사장님 대시보드에 오신 것을 환영합니다!",
  "description": "이 엔드포인트는 ROLE_EMPLOYER 권한이 있는 사용자만 접근할 수 있습니다."
}
```

**추가 테스트:**
- ❌ 토큰 없이 요청 → `401 Unauthorized`
- ❌ ROLE_EMPLOYER가 아닌 사용자 (향후 Employee 추가 시) → `403 Forbidden`

---

## ✅ 체크리스트

### 인증 테스트
- [ ] 회원가입 성공
- [ ] 중복 이메일 회원가입 실패
- [ ] 로그인 성공 → JWT 토큰 받기
- [ ] 잘못된 비밀번호로 로그인 실패
- [ ] 토큰 없이 `/api/employers/me` 접근 → 401
- [ ] 토큰과 함께 `/api/employers/me` 접근 → 200 + 사용자 정보
- [ ] 토큰과 함께 `/api/employers/profile` 접근 → 200

### 인가 테스트
- [ ] ROLE_EMPLOYER 토큰으로 `/api/employers/dashboard` 접근 → 200
- [ ] 토큰 없이 `/api/employers/dashboard` 접근 → 401

### Response DTO 테스트
- [ ] `/api/employers/me` 응답이 EmployerInfoResponse 형식인지 확인
- [ ] `/api/employers/dashboard` 응답이 DashboardResponse 형식인지 확인
- [ ] `/api/employers/profile` 응답이 ProfileResponse 형식인지 확인

---

## 🔧 Postman Collection 설정 팁

1. **환경 변수 설정:**
   - `base_url`: `http://localhost:8080`
   - `jwt_token`: (로그인 후 자동 설정)

2. **Pre-request Script (로그인 요청 후):**
   ```javascript
   if (pm.response.code === 200) {
       pm.environment.set("jwt_token", pm.response.text());
   }
   ```

3. **Authorization 설정:**
   - Type: Bearer Token
   - Token: `{{jwt_token}}`

---

## 📝 다음 단계 제안

현재 테스트 완료 후:
1. Employee 기능 구현 (회원가입/로그인)
2. 사장님이 직원 등록하는 기능
3. 스케줄 관리 기능
4. 근무 시간 기록 기능

