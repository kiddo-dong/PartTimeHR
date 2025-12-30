# Postman 테스트 가이드 (전체 기능)

## 🎯 테스트 시나리오

---

## 1단계: 사장님 회원가입 및 로그인

### 1.1 사장님 회원가입
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
**예상 응답**: `201 Created`

---

### 1.2 사장님 로그인
```
POST http://localhost:8080/api/employers/login
Content-Type: application/json

{
  "email": "boss@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK` + JWT 토큰
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib3NzQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfRU1QTE9ZRVIiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAzNjAwMH0.xxxxx
```

**⚠️ 중요**: 이 토큰을 변수에 저장하세요 (`employer_token`)

---

## 2단계: 사장님 정보 관리

### 2.1 사장님 정보 조회
```
GET http://localhost:8080/api/employers/me
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
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

---

### 2.2 사장님 정보 수정 (주간 시작 요일 설정 포함)
```
PUT http://localhost:8080/api/employers/me
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "name": "홍길동",
  "phone": "010-9999-8888",
  "storeName": "새로운 카페 이름",
  "weekStartDay": 2
}
```
**예상 응답**: `200 OK`
- `weekStartDay`: 1=월요일, 2=화요일, ..., 7=일요일

---

### 2.3 사장님 비밀번호 변경
```
PUT http://localhost:8080/api/employers/me
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "currentPassword": "password123",
  "password": "newpassword123",
  "passwordConfirm": "newpassword123"
}
```
**예상 응답**: `200 OK`

---

## 3단계: 직원 등록 및 관리

### 3.1 사장님이 직원 등록
```
POST http://localhost:8080/api/employers/employees
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "name": "김직원",
  "phone": "010-9876-5432"
}
```
**예상 응답**: `201 Created`

**추가 직원 등록** (여러 명 테스트용):
```json
{
  "email": "employee2@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "name": "이직원",
  "phone": "010-1111-2222"
}
```

---

### 3.2 직원 목록 조회
```
GET http://localhost:8080/api/employers/employees
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
```json
[
  {
    "id": 1,
    "email": "employee1@example.com",
    "name": "김직원",
    "phone": "010-9876-5432"
  },
  {
    "id": 2,
    "email": "employee2@example.com",
    "name": "이직원",
    "phone": "010-1111-2222"
  }
]
```

---

## 4단계: 직원 로그인 및 정보 관리

### 4.1 직원 로그인
```
POST http://localhost:8080/api/employees/login
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK` + JWT 토큰

**⚠️ 중요**: 이 토큰을 변수에 저장하세요 (`employee_token`)

---

### 4.2 직원 정보 조회
```
GET http://localhost:8080/api/employees/me
Authorization: Bearer {{employee_token}}
```
**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "email": "employee1@example.com",
  "name": "김직원",
  "phone": "010-9876-5432",
  "role": "ROLE_EMPLOYEE",
  "employerId": 1,
  "employerName": "홍길동",
  "storeName": "맛있는 카페",
  "createdAt": "2025-12-24T22:30:00"
}
```

---

### 4.3 직원 정보 수정
```
PUT http://localhost:8080/api/employees/me
Authorization: Bearer {{employee_token}}
Content-Type: application/json

{
  "name": "김직원수정",
  "phone": "010-7777-8888"
}
```
**예상 응답**: `200 OK`

---

## 5단계: 출근 기록 (매장 태블릿 방식)

### 5.1 직원 출근하기
```
POST http://localhost:8080/api/employers/work-records/clock-in
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `201 Created`
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": null,
  "breakEndTime": null,
  "clockOutTime": null,
  "status": "IN_PROGRESS",
  "totalWorkHours": null,
  "breakHours": 0.0,
  "actualWorkHours": null,
  "memo": null
}
```

**⚠️ 중요**: `record_id`를 저장할 필요 없음! 휴게/퇴근 시 직원 이메일/비밀번호만 입력하면 자동으로 오늘의 가장 최근 기록을 찾습니다.

---

### 5.2 직원의 오늘 기록 조회
```
POST http://localhost:8080/api/employers/work-records/today
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK` (가장 최근 기록 반환)

**⚠️ 중요**: `record_id`를 저장할 필요 없음! 이제 자동으로 찾습니다.

---

### 5.3 직원 휴게 시작 (record_id 불필요)
```
POST http://localhost:8080/api/employers/work-records/break-start
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "status": "ON_BREAK",
  "breakStartTime": "2025-12-24T12:00:00",
  ...
}
```

**동작 방식**: 직원 이메일/비밀번호로 오늘의 가장 최근 기록을 자동으로 찾아서 처리

---

### 5.4 직원 휴게 끝 (record_id 불필요)
```
POST http://localhost:8080/api/employers/work-records/break-end
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "status": "IN_PROGRESS",
  "breakEndTime": "2025-12-24T13:00:00",
  ...
}
```

**동작 방식**: 직원 이메일/비밀번호로 오늘의 가장 최근 기록을 자동으로 찾아서 처리

---

### 5.5 직원 퇴근하기 (record_id 불필요)
```
POST http://localhost:8080/api/employers/work-records/clock-out
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "status": "COMPLETED",
  "clockOutTime": "2025-12-24T18:00:00",
  "totalWorkHours": 9.0,
  "breakHours": 1.0,
  "actualWorkHours": 8.0,
  ...
}
```

---

## 6단계: 사장님이 출근 기록 직접 관리

### 6.1 출근 기록 수동 등록
```
POST http://localhost:8080/api/employers/work-records
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "employeeId": 1,
  "workDate": "2025-12-23",
  "clockInTime": "2025-12-23T09:00:00",
  "breakStartTime": "2025-12-23T12:00:00",
  "breakEndTime": "2025-12-23T13:00:00",
  "clockOutTime": "2025-12-23T18:00:00",
  "memo": "수동 등록"
}
```
**예상 응답**: `201 Created`

---

### 6.2 출근 기록 수정
```
PUT http://localhost:8080/api/employers/work-records/{{record_id}}
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "clockInTime": "2025-12-24T08:30:00",
  "clockOutTime": "2025-12-24T17:30:00",
  "memo": "수정된 메모"
}
```
**예상 응답**: `200 OK`
---

### 6.3 출근 기록 삭제
```
DELETE http://localhost:8080/api/employers/work-records/{{record_id}}
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `204 No Content`

---

### 6.4 전체 기록 조회
```
GET http://localhost:8080/api/employers/work-records
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK` (모든 직원의 모든 기록)

**특정 직원만 조회**:
```
GET http://localhost:8080/api/employers/work-records?employeeId=1
Authorization: Bearer {{employer_token}}
```

**날짜 범위로 조회**:
```
GET http://localhost:8080/api/employers/work-records?startDate=2025-12-01&endDate=2025-12-31
Authorization: Bearer {{employer_token}}
```

**직원 + 날짜 범위 조회**:
```
GET http://localhost:8080/api/employers/work-records?employeeId=1&startDate=2025-12-01&endDate=2025-12-31
Authorization: Bearer {{employer_token}}
```

---

### 6.5 특정 기록 조회
```
GET http://localhost:8080/api/employers/work-records/{{record_id}}
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`

---

## 7단계: 통계/리포트 기능

### 7.1 대시보드 통계
```
GET http://localhost:8080/api/employers/statistics/dashboard
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
```json
{
  "todayClockInCount": 2,
  "todayNotClockOutCount": 1,
  "todayTotalWorkHours": 9.0,
  "todayTotalActualWorkHours": 8.0,
  "thisMonthTotalWorkHours": 180.0,
  "thisMonthTotalActualWorkHours": 160.0,
  "thisMonthWorkDays": 20,
  "totalEmployeeCount": 2,
  "todayEmployees": [
    {
      "employeeId": 1,
      "employeeName": "김직원",
      "clockInTime": "2025-12-24T09:00:00",
      "clockOutTime": "2025-12-24T18:00:00",
      "status": "COMPLETED",
      "actualWorkHours": 8.0
    }
  ]
}
```

---

### 7.2 직원별 통계 (이번 달)
```
GET http://localhost:8080/api/employers/statistics/employees/1
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
```json
{
  "employeeId": 1,
  "employeeName": "김직원",
  "startDate": "2025-12-01",
  "endDate": "2025-12-24",
  "totalWorkDays": 20,
  "totalWorkHours": 180.0,
  "totalActualWorkHours": 160.0,
  "averageWorkHoursPerDay": 8.0
}
```

**특정 기간 통계**:
```
GET http://localhost:8080/api/employers/statistics/employees/1?startDate=2025-12-01&endDate=2025-12-15
Authorization: Bearer {{employer_token}}
```

---

### 7.3 월별 통계 (이번 달)
```
GET http://localhost:8080/api/employers/statistics/monthly
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
```json
{
  "year": 2025,
  "month": 12,
  "totalWorkDays": 20,
  "totalWorkHours": 360.0,
  "totalActualWorkHours": 320.0,
  "totalEmployeeCount": 2,
  "employeeSummaries": [
    {
      "employeeId": 1,
      "employeeName": "김직원",
      "workDays": 20,
      "totalWorkHours": 180.0,
      "totalActualWorkHours": 160.0,
      "averageWorkHoursPerDay": 8.0
    }
  ]
}
```

**특정 월 통계**:
```
GET http://localhost:8080/api/employers/statistics/monthly?year=2025&month=11
Authorization: Bearer {{employer_token}}
```

---

### 7.4 주간 통계 (이번 주)
```
GET http://localhost:8080/api/employers/statistics/weekly
Authorization: Bearer {{employer_token}}
```
**예상 응답**: `200 OK`
```json
{
  "weekStartDate": "2025-12-22",
  "weekEndDate": "2025-12-28",
  "weekStartDay": 1,
  "totalWorkDays": 5,
  "totalWorkHours": 40.0,
  "totalActualWorkHours": 35.0,
  "totalEmployeeCount": 2,
  "employeeSummaries": [
    {
      "employeeId": 1,
      "employeeName": "김직원",
      "workDays": 5,
      "totalWorkHours": 40.0,
      "totalActualWorkHours": 35.0,
      "averageWorkHoursPerDay": 7.0
    }
  ]
}
```

**특정 날짜 기준 주간 통계**:
```
GET http://localhost:8080/api/employers/statistics/weekly?date=2025-12-15
Authorization: Bearer {{employer_token}}
```

---

## 🔧 Postman 환경 변수 설정

### 환경 변수 생성
1. Postman에서 **Environments** → **+** 클릭
2. 환경 이름: `PartTimeHR Local`
3. 변수 추가:
   - `base_url`: `http://localhost:8080`
   - `employer_token`: (로그인 후 자동 설정)
   - `employee_token`: (로그인 후 자동 설정)
   - `record_id`: (사장님이 수동으로 기록 수정/삭제할 때만 필요, 매장 태블릿 출퇴근에는 불필요)

### Pre-request Script (로그인 요청에 추가)
```javascript
// 사장님 로그인 후
if (pm.response.code === 200) {
    pm.environment.set("employer_token", pm.response.text());
}

// 직원 로그인 후
if (pm.response.code === 200) {
    pm.environment.set("employee_token", pm.response.text());
}
```

### Authorization 설정
- Type: **Bearer Token**
- Token: `{{employer_token}}` 또는 `{{employee_token}}`

---

## 📋 테스트 체크리스트

### ✅ 인증/인가
- [ ] 사장님 회원가입
- [ ] 사장님 로그인 → 토큰 받기
- [ ] 직원 로그인 → 토큰 받기
- [ ] 토큰 없이 접근 → 401 에러
- [ ] 잘못된 토큰 → 401 에러

### ✅ 사장님 기능
- [ ] 사장님 정보 조회
- [ ] 사장님 정보 수정
- [ ] 주간 시작 요일 설정
- [ ] 비밀번호 변경
- [ ] 직원 등록
- [ ] 직원 목록 조회

### ✅ 직원 기능
- [ ] 직원 로그인
- [ ] 직원 정보 조회
- [ ] 직원 정보 수정

### ✅ 출근 기록 (매장 태블릿)
- [ ] 직원 출근
- [ ] 오늘 기록 조회
- [ ] 휴게 시작 (record_id 없이 자동 처리)
- [ ] 휴게 끝 (record_id 없이 자동 처리)
- [ ] 퇴근 (record_id 없이 자동 처리)
- [ ] 같은 날 여러 번 출근 가능한지 확인
- [ ] 다른 직원이 로그인해도 각자의 기록 자동 처리되는지 확인

### ✅ 출근 기록 관리
- [ ] 수동 등록
- [ ] 기록 수정
- [ ] 기록 삭제
- [ ] 전체 조회
- [ ] 특정 기록 조회
- [ ] 필터링 (직원별, 날짜별)

### ✅ 통계/리포트
- [ ] 대시보드 통계
- [ ] 직원별 통계
- [ ] 월별 통계
- [ ] 주간 통계 (주간 시작 요일 적용 확인)

---

## 🚨 에러 케이스 테스트

### 인증 에러
- 토큰 없이 요청 → `401 Unauthorized`
- 만료된 토큰 → `401 Unauthorized`
- 잘못된 토큰 형식 → `401 Unauthorized`

### 권한 에러
- 직원 토큰으로 사장님 전용 API 접근 → `403 Forbidden`
- 다른 사장님의 직원 기록 접근 → `400 Bad Request`

### 검증 에러
- 중복 이메일 회원가입 → `400 Bad Request`
- 비밀번호 불일치 → `400 Bad Request`
- 잘못된 날짜 형식 → `400 Bad Request`
- 주간 시작 요일 범위 초과 (8 이상) → `400 Bad Request`

---

## 💡 테스트 팁

1. **토큰 관리**: 로그인 후 토큰을 환경 변수에 저장
2. **record_id 관리**: 출근 기록 생성 후 `id` 값을 변수에 저장
3. **날짜 형식**: `YYYY-MM-DD` 형식 사용
4. **시간 형식**: `YYYY-MM-DDTHH:mm:ss` 형식 사용
5. **여러 직원 테스트**: 여러 직원을 등록하여 다양한 시나리오 테스트

---

**작성일**: 2025-12-24  
**버전**: 1.0

