# 출근/퇴근 기록 시스템 Postman 테스트 가이드

## 🎯 테스트 시나리오

---

## 1단계: 사전 준비 (회원가입 & 로그인)

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
**예상 응답**: `200 OK` + JWT 토큰 (사장님 토큰 저장해두기)

---

### 1.3 사장님이 직원 등록
```
POST http://localhost:8080/api/employers/employees
Authorization: Bearer {사장님_JWT_토큰}
Content-Type: application/json

{
  "email": "employee@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "name": "김직원",
  "phone": "010-9876-5432"
}
```
**예상 응답**: `201 Created`

---

### 1.4 직원 로그인
```
POST http://localhost:8080/api/employees/login
Content-Type: application/json

{
  "email": "employee@example.com",
  "password": "password123"
}
```
**예상 응답**: `200 OK` + JWT 토큰 (직원 토큰 저장해두기)

---

## 2단계: 직원 출근/퇴근 테스트 (간단한 버튼 클릭)

### 2.1 출근하기 ⏰
```
POST http://localhost:8080/api/employees/work-records/clock-in
Authorization: Bearer {직원_JWT_토큰}
```
**요청 본문**: 없음 (자동으로 현재 시간 기록)

**예상 응답**: `201 Created`
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-25",
  "clockInTime": "2025-12-25T09:00:00",
  "breakStartTime": null,
  "breakEndTime": null,
  "clockOutTime": null,
  "status": "IN_PROGRESS",
  "totalWorkHours": null,
  "breakHours": 0.0,
  "actualWorkHours": null,
  "memo": null,
  "createdAt": "2025-12-25T09:00:00",
  "updatedAt": "2025-12-25T09:00:00"
}
```
**중요**: `id` 값을 복사해두세요! (휴게/퇴근 시 사용)

---

### 2.2 오늘 출근 기록 조회 (현재 상태 확인)
```
GET http://localhost:8080/api/employees/work-records/today
Authorization: Bearer {직원_JWT_토큰}
```
**예상 응답**: `200 OK` + 위와 동일한 JSON

---

### 2.3 휴게 시작 🍽️
```
POST http://localhost:8080/api/employees/work-records/{recordId}/break-start
Authorization: Bearer {직원_JWT_토큰}
```
**{recordId}**: 위에서 받은 `id` 값 (예: 1)

**요청 본문**: 없음

**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-25",
  "clockInTime": "2025-12-25T09:00:00",
  "breakStartTime": "2025-12-25T13:00:00",
  "breakEndTime": null,
  "clockOutTime": null,
  "status": "ON_BREAK",
  "totalWorkHours": null,
  "breakHours": 0.0,
  "actualWorkHours": null,
  "memo": null,
  "createdAt": "2025-12-25T09:00:00",
  "updatedAt": "2025-12-25T13:00:00"
}
```
**변경사항**: `breakStartTime`이 기록되고, `status`가 `ON_BREAK`로 변경됨

---

### 2.4 휴게 끝 🔄
```
POST http://localhost:8080/api/employees/work-records/{recordId}/break-end
Authorization: Bearer {직원_JWT_토큰}
```
**{recordId}**: 같은 `id` 값

**요청 본문**: 없음

**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-25",
  "clockInTime": "2025-12-25T09:00:00",
  "breakStartTime": "2025-12-25T13:00:00",
  "breakEndTime": "2025-12-25T14:00:00",
  "clockOutTime": null,
  "status": "IN_PROGRESS",
  "totalWorkHours": null,
  "breakHours": 1.0,
  "actualWorkHours": null,
  "memo": null,
  "createdAt": "2025-12-25T09:00:00",
  "updatedAt": "2025-12-25T14:00:00"
}
```
**변경사항**: `breakEndTime`이 기록되고, `status`가 `IN_PROGRESS`로 변경, `breakHours`가 1.0으로 계산됨

---

### 2.5 퇴근하기 🏠
```
POST http://localhost:8080/api/employees/work-records/{recordId}/clock-out
Authorization: Bearer {직원_JWT_토큰}
```
**{recordId}**: 같은 `id` 값

**요청 본문**: 없음

**예상 응답**: `200 OK`
```json
{
  "id": 1,
  "employeeId": 1,
  "employeeName": "김직원",
  "workDate": "2025-12-25",
  "clockInTime": "2025-12-25T09:00:00",
  "breakStartTime": "2025-12-25T13:00:00",
  "breakEndTime": "2025-12-25T14:00:00",
  "clockOutTime": "2025-12-25T18:00:00",
  "status": "COMPLETED",
  "totalWorkHours": 9.0,
  "breakHours": 1.0,
  "actualWorkHours": 8.0,
  "memo": null,
  "createdAt": "2025-12-25T09:00:00",
  "updatedAt": "2025-12-25T18:00:00"
}
```
**변경사항**: 
- `clockOutTime`이 기록됨
- `status`가 `COMPLETED`로 변경
- `totalWorkHours`: 9.0 (09:00 ~ 18:00)
- `breakHours`: 1.0 (13:00 ~ 14:00)
- `actualWorkHours`: 8.0 (9 - 1)

---

### 2.6 내 기록 조회 (전체)
```
GET http://localhost:8080/api/employees/work-records
Authorization: Bearer {직원_JWT_토큰}
```
**예상 응답**: `200 OK` + 기록 목록 배열

---

### 2.7 내 기록 조회 (날짜 범위)
```
GET http://localhost:8080/api/employees/work-records?startDate=2025-12-01&endDate=2025-12-31
Authorization: Bearer {직원_JWT_토큰}
```
**예상 응답**: `200 OK` + 해당 기간의 기록 목록

---

## 3단계: 고용주 관리 테스트 (자유로운 수정/삭제)

### 3.1 전체 출근 기록 조회
```
GET http://localhost:8080/api/employers/work-records
Authorization: Bearer {사장님_JWT_토큰}
```
**예상 응답**: `200 OK` + 모든 직원의 기록 목록

---

### 3.2 특정 직원의 기록 조회
```
GET http://localhost:8080/api/employers/work-records?employeeId=1
Authorization: Bearer {사장님_JWT_토큰}
```
**예상 응답**: `200 OK` + 해당 직원의 기록 목록

---

### 3.3 날짜 범위로 조회
```
GET http://localhost:8080/api/employers/work-records?startDate=2025-12-01&endDate=2025-12-31
Authorization: Bearer {사장님_JWT_토큰}
```
**예상 응답**: `200 OK` + 해당 기간의 기록 목록

---

### 3.4 특정 기록 조회
```
GET http://localhost:8080/api/employers/work-records/{recordId}
Authorization: Bearer {사장님_JWT_토큰}
```
**{recordId}**: 위에서 받은 기록의 `id` 값

**예상 응답**: `200 OK` + 기록 상세 정보

---

### 3.5 출근 기록 수정 (잘못 입력된 시간 수정)
```
PUT http://localhost:8080/api/employers/work-records/{recordId}
Authorization: Bearer {사장님_JWT_토큰}
Content-Type: application/json

{
  "clockInTime": "2025-12-25T08:30:00",
  "breakStartTime": "2025-12-25T13:00:00",
  "breakEndTime": "2025-12-25T14:00:00",
  "clockOutTime": "2025-12-25T18:00:00",
  "memo": "출근 시간 수정함 (09:00 → 08:30)"
}
```
**예상 응답**: `200 OK` + 수정된 기록 정보
- `totalWorkHours`: 9.5시간으로 재계산
- `actualWorkHours`: 8.5시간으로 재계산

**부분 수정 예시** (출근 시간만 수정):
```json
{
  "clockInTime": "2025-12-25T08:30:00"
}
```
→ 나머지 필드는 그대로 유지되고, 출근 시간만 변경됨

---

### 3.6 출근 기록 삭제
```
DELETE http://localhost:8080/api/employers/work-records/{recordId}
Authorization: Bearer {사장님_JWT_토큰}
```
**예상 응답**: `204 No Content`

---

### 3.7 출근 기록 수동 등록 (과거 날짜 등록)
```
POST http://localhost:8080/api/employers/work-records
Authorization: Bearer {사장님_JWT_토큰}
Content-Type: application/json

{
  "employeeId": 1,
  "workDate": "2025-12-24",
  "clockInTime": "2025-12-24T09:00:00",
  "breakStartTime": "2025-12-24T13:00:00",
  "breakEndTime": "2025-12-24T14:00:00",
  "clockOutTime": "2025-12-24T18:00:00",
  "memo": "수동 등록"
}
```
**예상 응답**: `201 Created` + 생성된 기록 정보

---

## 4단계: 에러 케이스 테스트

### 4.1 중복 출근 시도
```
POST http://localhost:8080/api/employees/work-records/clock-in
Authorization: Bearer {직원_JWT_토큰}
```
**이미 오늘 출근한 경우**

**예상 응답**: `400 Bad Request`
```json
{
  "message": "오늘 이미 출근 기록이 있습니다."
}
```

---

### 4.2 잘못된 상태에서 휴게 시작
```
POST http://localhost:8080/api/employees/work-records/{recordId}/break-start
Authorization: Bearer {직원_JWT_토큰}
```
**이미 휴게 중이거나 퇴근한 경우**

**예상 응답**: `400 Bad Request`
```json
{
  "message": "근무 중일 때만 휴게를 시작할 수 있습니다."
}
```

---

### 4.3 다른 사람의 기록 수정 시도
```
POST http://localhost:8080/api/employees/work-records/{다른사람의_recordId}/break-start
Authorization: Bearer {직원_JWT_토큰}
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "자신의 출근 기록만 수정할 수 있습니다."
}
```

---

### 4.4 고용주가 다른 사람의 직원 기록 수정 시도
```
PUT http://localhost:8080/api/employers/work-records/{다른사장님의_직원_recordId}
Authorization: Bearer {사장님_JWT_토큰}
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "자신의 직원의 출근 기록만 수정할 수 있습니다."
}
```

---

### 4.5 시간 순서 오류
```
PUT http://localhost:8080/api/employers/work-records/{recordId}
Authorization: Bearer {사장님_JWT_토큰}
Content-Type: application/json

{
  "clockInTime": "2025-12-25T18:00:00",
  "clockOutTime": "2025-12-25T09:00:00"
}
```
**예상 응답**: `400 Bad Request`
```json
{
  "message": "출근 시간은 퇴근 시간보다 이전이어야 합니다."
}
```

---

## ✅ 테스트 체크리스트

### 직원 기능
- [ ] 출근하기 성공
- [ ] 중복 출근 시도 → 실패
- [ ] 휴게 시작 성공
- [ ] 잘못된 상태에서 휴게 시작 → 실패
- [ ] 휴게 끝 성공
- [ ] 퇴근하기 성공
- [ ] 오늘 기록 조회 성공
- [ ] 내 기록 조회 성공

### 고용주 기능
- [ ] 전체 기록 조회 성공
- [ ] 특정 직원 기록 조회 성공
- [ ] 기록 수정 성공
- [ ] 부분 수정 성공 (일부 필드만)
- [ ] 기록 삭제 성공
- [ ] 수동 등록 성공
- [ ] 다른 사람의 직원 기록 수정 시도 → 실패

### 근무 시간 계산
- [ ] 퇴근 후 총 근무 시간 계산 확인
- [ ] 휴게 시간 계산 확인
- [ ] 실제 근무 시간 계산 확인 (총 - 휴게)

---

## 🔧 Postman Collection 설정 팁

### 환경 변수
```
base_url: http://localhost:8080
employee_token: (직원 로그인 후 자동 설정)
employer_token: (사장님 로그인 후 자동 설정)
record_id: (출근 후 자동 설정)
```

### Pre-request Script (출근 요청 후)
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("record_id", jsonData.id);
}
```

---

## 📝 테스트 시나리오 예시

### 완전한 하루 근무 시나리오
1. 직원 로그인
2. 출근하기 → `record_id` 저장
3. 오늘 기록 조회 → 상태 확인
4. 휴게 시작
5. 휴게 끝
6. 퇴근하기 → 근무 시간 확인
7. 사장님 로그인
8. 전체 기록 조회
9. 기록 수정 (잘못 입력된 시간)
10. 수정 후 근무 시간 재계산 확인

---

**테스트 시작하세요!** 🚀

