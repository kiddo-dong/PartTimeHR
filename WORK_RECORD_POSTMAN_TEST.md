# 출근 기록 Postman 테스트 가이드

## 🎯 전제 조건

이 가이드를 사용하기 전에 다음이 완료되어 있어야 합니다:
1. ✅ 사장님 회원가입 및 로그인 완료
2. ✅ 직원 등록 완료
3. ✅ 사장님 JWT 토큰 보유 (`employer_token`)

---

## 📋 출근 기록 테스트 시나리오

### 1. 직원 출근하기

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
  "memo": null,
  "createdAt": "2025-12-24T09:00:00",
  "updatedAt": "2025-12-24T09:00:00"
}
```

**✅ 확인 사항**:
- `status`가 `IN_PROGRESS`인지 확인
- `clockInTime`이 현재 시간인지 확인

---

### 2. 직원의 오늘 기록 조회

```
POST http://localhost:8080/api/employers/work-records/today
Authorization: Bearer {{employer_token}}
Content-Type: application/json

{
  "email": "employee1@example.com",
  "password": "password123"
}
```

**예상 응답**: `200 OK`
- 오늘의 가장 최근 출근 기록 반환
- 여러 번 출근한 경우 가장 최근 기록 반환

**출근 기록이 없는 경우**: `204 No Content`

---

### 3. 직원 휴게 시작

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
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": "2025-12-24T12:00:00",
  "breakEndTime": null,
  "clockOutTime": null,
  "status": "ON_BREAK",
  "totalWorkHours": null,
  "breakHours": 0.0,
  "actualWorkHours": null,
  "memo": null
}
```

**✅ 확인 사항**:
- `status`가 `ON_BREAK`로 변경되었는지 확인
- `breakStartTime`이 현재 시간인지 확인

**⚠️ 에러 케이스**:
- 출근하지 않은 경우: `"오늘 출근 기록이 없습니다. 먼저 출근해주세요."`
- 이미 휴게 중인 경우: `"휴게 중일 때만 휴게를 종료할 수 있습니다."`
- 이미 퇴근한 경우: `"이미 퇴근했습니다."`

---

### 4. 직원 휴게 끝

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
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": "2025-12-24T12:00:00",
  "breakEndTime": "2025-12-24T13:00:00",
  "clockOutTime": null,
  "status": "IN_PROGRESS",
  "totalWorkHours": null,
  "breakHours": 1.0,
  "actualWorkHours": null,
  "memo": null
}
```

**✅ 확인 사항**:
- `status`가 `IN_PROGRESS`로 변경되었는지 확인
- `breakEndTime`이 현재 시간인지 확인
- `breakHours`가 계산되었는지 확인 (1.0시간)

**⚠️ 에러 케이스**:
- 출근하지 않은 경우: `"오늘 출근 기록이 없습니다. 먼저 출근해주세요."`
- 휴게 중이 아닌 경우: `"휴게 중일 때만 휴게를 종료할 수 있습니다."`

---

### 5. 직원 퇴근하기

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
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": "2025-12-24T12:00:00",
  "breakEndTime": "2025-12-24T13:00:00",
  "clockOutTime": "2025-12-24T18:00:00",
  "status": "COMPLETED",
  "totalWorkHours": 9.0,
  "breakHours": 1.0,
  "actualWorkHours": 8.0,
  "memo": null
}
```

**✅ 확인 사항**:
- `status`가 `COMPLETED`로 변경되었는지 확인
- `clockOutTime`이 현재 시간인지 확인
- `totalWorkHours`가 계산되었는지 확인 (9.0시간)
- `breakHours`가 계산되었는지 확인 (1.0시간)
- `actualWorkHours`가 계산되었는지 확인 (8.0시간 = 9.0 - 1.0)

**⚠️ 에러 케이스**:
- 출근하지 않은 경우: `"오늘 출근 기록이 없습니다. 먼저 출근해주세요."`
- 이미 퇴근한 경우: `"이미 퇴근했습니다."`

---

## 🔄 전체 플로우 테스트

### 시나리오 1: 기본 출퇴근 (휴게 없음)
```
1. 출근 → status: IN_PROGRESS
2. 퇴근 → status: COMPLETED, totalWorkHours: 9.0, actualWorkHours: 9.0
```

### 시나리오 2: 출퇴근 + 휴게
```
1. 출근 → status: IN_PROGRESS
2. 휴게 시작 → status: ON_BREAK
3. 휴게 끝 → status: IN_PROGRESS
4. 퇴근 → status: COMPLETED, totalWorkHours: 9.0, breakHours: 1.0, actualWorkHours: 8.0
```

### 시나리오 3: 같은 날 여러 번 출근
```
1. 직원 A 출근 (오전 근무) → record_id: 1
2. 직원 A 퇴근 → record_id: 1 완료
3. 직원 A 출근 (오후 근무) → record_id: 2 (새로운 기록)
4. 직원 A 퇴근 → record_id: 2 완료
```

---

## 👥 여러 직원 테스트

### 시나리오: 다른 직원이 같은 태블릿 사용

**직원 1 (김직원)**:
```
1. POST /clock-in
   Body: { "email": "employee1@example.com", "password": "password123" }
   → 직원 1의 기록 생성

2. POST /break-start
   Body: { "email": "employee1@example.com", "password": "password123" }
   → 직원 1의 오늘 가장 최근 기록에 휴게 시작
```

**직원 2 (이직원)**:
```
1. POST /clock-in
   Body: { "email": "employee2@example.com", "password": "password123" }
   → 직원 2의 기록 생성 (직원 1과 독립적)

2. POST /break-start
   Body: { "email": "employee2@example.com", "password": "password123" }
   → 직원 2의 오늘 가장 최근 기록에 휴게 시작 (직원 1과 독립적)
```

**✅ 확인 사항**:
- 각 직원의 기록이 독립적으로 관리되는지 확인
- 다른 직원의 기록에 영향을 주지 않는지 확인

---

## 🚨 에러 케이스 테스트

### 1. 출근하지 않고 휴게 시작 시도
```
POST /api/employers/work-records/break-start
Body: { "email": "employee1@example.com", "password": "password123" }
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "오늘 출근 기록이 없습니다. 먼저 출근해주세요."
}
```

### 2. 잘못된 비밀번호
```
POST /api/employers/work-records/clock-in
Body: { "email": "employee1@example.com", "password": "wrongpassword" }
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "비밀번호가 일치하지 않습니다."
}
```

### 3. 다른 사장님의 직원 시도
```
POST /api/employers/work-records/clock-in
Body: { "email": "other_employee@example.com", "password": "password123" }
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "자신의 직원만 출근할 수 있습니다."
}
```

### 4. 이미 퇴근한 후 다시 휴게 시작 시도
```
1. 출근
2. 퇴근
3. 휴게 시작 시도
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "근무 중일 때만 휴게를 시작할 수 있습니다."
}
```

---

## 💡 테스트 팁

1. **순서대로 테스트**: 출근 → 휴게 시작 → 휴게 끝 → 퇴근 순서로 테스트
2. **여러 직원 테스트**: 여러 직원을 등록하여 각각 독립적으로 동작하는지 확인
3. **같은 날 여러 번 출근**: 같은 직원이 같은 날 여러 번 출근 가능한지 확인
4. **시간 계산 확인**: 퇴근 후 `totalWorkHours`, `breakHours`, `actualWorkHours`가 올바르게 계산되는지 확인
5. **상태 확인**: 각 단계에서 `status`가 올바르게 변경되는지 확인

---

## 📊 예상 결과

### 완전한 출퇴근 플로우 후
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": "2025-12-24T12:00:00",
  "breakEndTime": "2025-12-24T13:00:00",
  "clockOutTime": "2025-12-24T18:00:00",
  "status": "COMPLETED",
  "totalWorkHours": 9.0,      // 09:00 ~ 18:00 = 9시간
  "breakHours": 1.0,          // 12:00 ~ 13:00 = 1시간
  "actualWorkHours": 8.0,     // 9.0 - 1.0 = 8시간
  "memo": null
}
```

---

**작성일**: 2025-12-24  
**버전**: 2.0 (record_id 제거 버전)

