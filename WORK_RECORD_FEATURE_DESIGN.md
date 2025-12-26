# 출근/퇴근 기록 시스템 설계 문서

## 📋 기능 개요

**목적**: 간단하고 빠른 출근/퇴근 관리 시스템

직원은 매장에 와서 버튼만 누르면 출근/퇴근이 기록되고, 고용주는 언제든지 기록을 수정/삭제할 수 있습니다.

---

## 🗄️ 데이터베이스 설계

### WorkRecord 엔티티 (출근 기록)

```java
@Entity
@Table(name = "work_record")
public class WorkRecord {
    - id: Long (PK)
    - employee: Employee (ManyToOne) - 어떤 직원의 기록인지
    - workDate: LocalDate - 출근 날짜
    - clockInTime: LocalDateTime - 출근 시간 (필수)
    - breakStartTime: LocalDateTime - 휴게 시작 시간 (선택, nullable)
    - breakEndTime: LocalDateTime - 휴게 끝 시간 (선택, nullable)
    - clockOutTime: LocalDateTime - 퇴근 시간 (선택, nullable)
    - status: WorkStatus (enum) - 근무 상태
        - IN_PROGRESS: 근무 중 (출근만 함)
        - ON_BREAK: 휴게 중 (휴게 시작함)
        - COMPLETED: 근무 완료 (퇴근함)
    - memo: String - 메모 (고용주가 수정 시 사용)
    - createdAt: LocalDateTime
    - updatedAt: LocalDateTime
}
```

### 관계
- WorkRecord : Employee = N : 1 (한 직원은 여러 출근 기록을 가질 수 있음)
- WorkRecord는 Employee를 통해 Employer와 연결됨

---

## 🎯 기능 요구사항

### 1. 직원 기능 (ROLE_EMPLOYEE) - 간단한 버튼 클릭

#### 1.1 출근하기
- **엔드포인트**: `POST /api/employees/work-records/clock-in`
- **권한**: ROLE_EMPLOYEE
- **요청 본문**: 없음 (자동으로 현재 시간 기록)
- **동작**:
  - 현재 시간을 `clockInTime`에 저장
  - `workDate`는 오늘 날짜로 자동 설정
  - `status`를 `IN_PROGRESS`로 설정
  - 같은 날짜에 이미 출근 기록이 있으면 에러
- **응답**: `201 Created` + 출근 기록 정보

#### 1.2 휴게 시작
- **엔드포인트**: `POST /api/employees/work-records/{recordId}/break-start`
- **권한**: ROLE_EMPLOYEE
- **요청 본문**: 없음
- **동작**:
  - 현재 시간을 `breakStartTime`에 저장
  - `status`를 `ON_BREAK`로 변경
  - 자신의 오늘 출근 기록이어야 함
  - 이미 휴게 중이면 에러
- **응답**: `200 OK` + 업데이트된 기록 정보

#### 1.3 휴게 끝
- **엔드포인트**: `POST /api/employees/work-records/{recordId}/break-end`
- **권한**: ROLE_EMPLOYEE
- **요청 본문**: 없음
- **동작**:
  - 현재 시간을 `breakEndTime`에 저장
  - `status`를 `IN_PROGRESS`로 변경
  - 자신의 오늘 출근 기록이어야 함
  - 휴게 중이 아니면 에러
- **응답**: `200 OK` + 업데이트된 기록 정보

#### 1.4 퇴근하기
- **엔드포인트**: `POST /api/employees/work-records/{recordId}/clock-out`
- **권한**: ROLE_EMPLOYEE
- **요청 본문**: 없음
- **동작**:
  - 현재 시간을 `clockOutTime`에 저장
  - `status`를 `COMPLETED`로 변경
  - 자신의 오늘 출근 기록이어야 함
  - 이미 퇴근했으면 에러
- **응답**: `200 OK` + 완료된 기록 정보 (총 근무 시간 포함)

#### 1.5 내 출근 기록 조회
- **엔드포인트**: `GET /api/employees/work-records`
- **권한**: ROLE_EMPLOYEE
- **쿼리 파라미터**:
  - `startDate` (선택): 시작 날짜
  - `endDate` (선택): 종료 날짜
- **응답**: `200 OK` + 자신의 출근 기록 목록

#### 1.6 오늘 출근 기록 조회
- **엔드포인트**: `GET /api/employees/work-records/today`
- **권한**: ROLE_EMPLOYEE
- **응답**: `200 OK` + 오늘의 출근 기록 (없으면 null)

---

### 2. 고용주 기능 (ROLE_EMPLOYER) - 자유로운 수정/삭제

#### 2.1 출근 기록 수정
- **엔드포인트**: `PUT /api/employers/work-records/{recordId}`
- **권한**: ROLE_EMPLOYER
- **요청 본문**:
  ```json
  {
    "clockInTime": "2025-12-25T09:00:00",
    "breakStartTime": "2025-12-25T13:00:00",  // nullable
    "breakEndTime": "2025-12-25T14:00:00",    // nullable
    "clockOutTime": "2025-12-25T18:00:00",
    "memo": "출근 시간 수정함"
  }
  ```
- **검증 규칙**:
  - 자신의 직원의 기록인지 확인
  - 시간 순서: clockInTime < breakStartTime < breakEndTime < clockOutTime
  - 모든 필드 선택 가능 (부분 수정 가능)
- **응답**: `200 OK` + 수정된 기록 정보

#### 2.2 출근 기록 삭제
- **엔드포인트**: `DELETE /api/employers/work-records/{recordId}`
- **권한**: ROLE_EMPLOYER
- **검증 규칙**: 자신의 직원의 기록인지 확인
- **응답**: `204 No Content`

#### 2.3 출근 기록 수동 등록
- **엔드포인트**: `POST /api/employers/work-records`
- **권한**: ROLE_EMPLOYER
- **요청 본문**:
  ```json
  {
    "employeeId": 1,
    "workDate": "2025-12-25",
    "clockInTime": "2025-12-25T09:00:00",
    "breakStartTime": "2025-12-25T13:00:00",  // 선택
    "breakEndTime": "2025-12-25T14:00:00",    // 선택
    "clockOutTime": "2025-12-25T18:00:00",
    "memo": "수동 등록"
  }
  ```
- **응답**: `201 Created` + 생성된 기록 정보

#### 2.4 전체 출근 기록 조회
- **엔드포인트**: `GET /api/employers/work-records`
- **권한**: ROLE_EMPLOYER
- **쿼리 파라미터**:
  - `employeeId` (선택): 특정 직원의 기록만
  - `startDate` (선택): 시작 날짜
  - `endDate` (선택): 종료 날짜
- **응답**: `200 OK` + 출근 기록 목록 (직원 정보 포함)

#### 2.5 특정 출근 기록 조회
- **엔드포인트**: `GET /api/employers/work-records/{recordId}`
- **권한**: ROLE_EMPLOYER
- **응답**: `200 OK` + 출근 기록 상세 정보

---

## 📊 API 엔드포인트 요약

### 직원 (ROLE_EMPLOYEE) - 간단한 버튼 클릭
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/employees/work-records/clock-in` | 출근하기 | ROLE_EMPLOYEE |
| POST | `/api/employees/work-records/{id}/break-start` | 휴게 시작 | ROLE_EMPLOYEE |
| POST | `/api/employees/work-records/{id}/break-end` | 휴게 끝 | ROLE_EMPLOYEE |
| POST | `/api/employees/work-records/{id}/clock-out` | 퇴근하기 | ROLE_EMPLOYEE |
| GET | `/api/employees/work-records` | 내 기록 조회 | ROLE_EMPLOYEE |
| GET | `/api/employees/work-records/today` | 오늘 기록 조회 | ROLE_EMPLOYEE |

### 고용주 (ROLE_EMPLOYER) - 자유로운 관리
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/employers/work-records` | 기록 수동 등록 | ROLE_EMPLOYER |
| PUT | `/api/employers/work-records/{id}` | 기록 수정 | ROLE_EMPLOYER |
| DELETE | `/api/employers/work-records/{id}` | 기록 삭제 | ROLE_EMPLOYER |
| GET | `/api/employers/work-records` | 전체 기록 조회 | ROLE_EMPLOYER |
| GET | `/api/employers/work-records/{id}` | 특정 기록 조회 | ROLE_EMPLOYER |

---

## 📝 DTO 설계

### Request DTO

#### CreateWorkRecordRequest (고용주용)
```java
{
    employeeId: Long (필수)
    workDate: LocalDate (필수) - "2025-12-25"
    clockInTime: LocalDateTime (필수) - "2025-12-25T09:00:00"
    breakStartTime: LocalDateTime (선택) - "2025-12-25T13:00:00"
    breakEndTime: LocalDateTime (선택) - "2025-12-25T14:00:00"
    clockOutTime: LocalDateTime (선택) - "2025-12-25T18:00:00"
    memo: String (선택)
}
```

#### UpdateWorkRecordRequest (고용주용)
```java
{
    clockInTime: LocalDateTime (선택)
    breakStartTime: LocalDateTime (선택)
    breakEndTime: LocalDateTime (선택)
    clockOutTime: LocalDateTime (선택)
    memo: String (선택)
}
// 모든 필드가 선택 가능 (부분 수정)
```

### Response DTO

#### WorkRecordResponse
```java
{
    id: Long
    employeeId: Long
    employeeName: String
    workDate: LocalDate
    clockInTime: LocalDateTime
    breakStartTime: LocalDateTime (nullable)
    breakEndTime: LocalDateTime (nullable)
    clockOutTime: LocalDateTime (nullable)
    status: String (IN_PROGRESS, ON_BREAK, COMPLETED)
    totalWorkHours: Double (총 근무 시간, 자동 계산)
    breakHours: Double (휴게 시간, 자동 계산)
    actualWorkHours: Double (실제 근무 시간 = 총 - 휴게, 자동 계산)
    memo: String
    createdAt: LocalDateTime
    updatedAt: LocalDateTime
}
```

---

## 🔍 비즈니스 로직 상세

### 1. 직원 출근하기 (Clock In)

#### 1.1 자동 시간 기록
- **규칙**: 현재 시간을 자동으로 `clockInTime`에 저장
- **로직**:
  ```java
  LocalDateTime now = LocalDateTime.now();
  workRecord.setClockInTime(now);
  workRecord.setWorkDate(now.toLocalDate());
  workRecord.setStatus(WorkStatus.IN_PROGRESS);
  ```

#### 1.2 중복 출근 방지
- **규칙**: 같은 날짜에 이미 출근 기록이 있으면 에러
- **검증 로직**:
  ```java
  LocalDate today = LocalDate.now();
  if (workRecordRepository.existsByEmployeeAndWorkDate(employee, today)) {
      throw new IllegalArgumentException("오늘 이미 출근 기록이 있습니다.");
  }
  ```

#### 1.3 오늘 기록 조회
- **규칙**: 출근 후에는 오늘 기록 ID를 반환하여 휴게/퇴근 시 사용
- **응답**: `WorkRecordResponse`에 `id` 포함

---

### 2. 직원 휴게 시작 (Break Start)

#### 2.1 상태 검증
- **규칙**: `IN_PROGRESS` 상태여야 함
- **검증 로직**:
  ```java
  if (workRecord.getStatus() != WorkStatus.IN_PROGRESS) {
      throw new IllegalArgumentException("근무 중일 때만 휴게를 시작할 수 있습니다.");
  }
  ```

#### 2.2 시간 기록
- **규칙**: 현재 시간을 `breakStartTime`에 저장
- **로직**:
  ```java
  workRecord.setBreakStartTime(LocalDateTime.now());
  workRecord.setStatus(WorkStatus.ON_BREAK);
  ```

#### 2.3 소유권 검증
- **규칙**: 자신의 오늘 출근 기록이어야 함
- **검증**: 직원 ID와 오늘 날짜 확인

---

### 3. 직원 휴게 끝 (Break End)

#### 3.1 상태 검증
- **규칙**: `ON_BREAK` 상태여야 함
- **검증 로직**:
  ```java
  if (workRecord.getStatus() != WorkStatus.ON_BREAK) {
      throw new IllegalArgumentException("휴게 중일 때만 휴게를 종료할 수 있습니다.");
  }
  ```

#### 3.2 시간 기록
- **규칙**: 현재 시간을 `breakEndTime`에 저장
- **로직**:
  ```java
  workRecord.setBreakEndTime(LocalDateTime.now());
  workRecord.setStatus(WorkStatus.IN_PROGRESS);
  ```

#### 3.3 시간 순서 검증
- **규칙**: `breakEndTime` > `breakStartTime`
- **검증**: 자동으로 현재 시간이므로 항상 만족

---

### 4. 직원 퇴근하기 (Clock Out)

#### 4.1 상태 검증
- **규칙**: `IN_PROGRESS` 또는 `ON_BREAK` 상태여야 함
- **검증 로직**:
  ```java
  if (workRecord.getStatus() == WorkStatus.COMPLETED) {
      throw new IllegalArgumentException("이미 퇴근했습니다.");
  }
  ```

#### 4.2 시간 기록
- **규칙**: 현재 시간을 `clockOutTime`에 저장
- **로직**:
  ```java
  workRecord.setClockOutTime(LocalDateTime.now());
  workRecord.setStatus(WorkStatus.COMPLETED);
  ```

#### 4.3 근무 시간 계산
- **규칙**: 퇴근 시 총 근무 시간 자동 계산
- **로직**: 아래 "근무 시간 계산" 참고

---

### 5. 고용주 기록 수정 (Update)

#### 5.1 자유로운 수정
- **규칙**: 모든 필드를 자유롭게 수정 가능
- **특징**:
  - 부분 수정 가능 (일부 필드만 수정해도 됨)
  - 과거 날짜도 수정 가능
  - 완료된 기록도 수정 가능

#### 5.2 시간 순서 검증
- **규칙**: 시간 순서가 올바른지 검증
- **검증 로직**:
  ```java
  if (clockInTime != null && breakStartTime != null) {
      if (!clockInTime.isBefore(breakStartTime)) {
          throw new IllegalArgumentException("출근 시간은 휴게 시작 시간보다 이전이어야 합니다.");
      }
  }
  if (breakStartTime != null && breakEndTime != null) {
      if (!breakStartTime.isBefore(breakEndTime)) {
          throw new IllegalArgumentException("휴게 시작 시간은 휴게 끝 시간보다 이전이어야 합니다.");
      }
  }
  if (breakEndTime != null && clockOutTime != null) {
      if (!breakEndTime.isBefore(clockOutTime)) {
          throw new IllegalArgumentException("휴게 끝 시간은 퇴근 시간보다 이전이어야 합니다.");
      }
  }
  if (clockInTime != null && clockOutTime != null) {
      if (!clockInTime.isBefore(clockOutTime)) {
          throw new IllegalArgumentException("출근 시간은 퇴근 시간보다 이전이어야 합니다.");
      }
  }
  ```

#### 5.3 상태 자동 업데이트
- **규칙**: 수정 후 상태를 자동으로 업데이트
- **로직**:
  ```java
  if (clockOutTime != null) {
      status = WorkStatus.COMPLETED;
  } else if (breakStartTime != null && breakEndTime == null) {
      status = WorkStatus.ON_BREAK;
  } else if (clockInTime != null) {
      status = WorkStatus.IN_PROGRESS;
  }
  ```

#### 5.4 소유권 검증
- **규칙**: 자신의 직원의 기록만 수정 가능

---

### 6. 고용주 기록 삭제 (Delete)

#### 6.1 자유로운 삭제
- **규칙**: 언제든지 삭제 가능 (완료된 기록도 삭제 가능)
- **이유**: 잘못 입력된 기록을 자유롭게 삭제

#### 6.2 소유권 검증
- **규칙**: 자신의 직원의 기록만 삭제 가능

---

### 7. 근무 시간 계산

#### 7.1 총 근무 시간 (Total Work Hours)
- **규칙**: 출근 시간부터 퇴근 시간까지
- **로직**:
  ```java
  if (clockOutTime != null && clockInTime != null) {
      Duration duration = Duration.between(clockInTime, clockOutTime);
      totalWorkHours = duration.toMinutes() / 60.0;
  }
  ```
- **예시**: 09:00 출근, 18:00 퇴근 → 9시간

#### 7.2 휴게 시간 (Break Hours)
- **규칙**: 휴게 시작부터 휴게 끝까지
- **로직**:
  ```java
  if (breakStartTime != null && breakEndTime != null) {
      Duration breakDuration = Duration.between(breakStartTime, breakEndTime);
      breakHours = breakDuration.toMinutes() / 60.0;
  } else {
      breakHours = 0.0;
  }
  ```
- **예시**: 13:00 휴게 시작, 14:00 휴게 끝 → 1시간

#### 7.3 실제 근무 시간 (Actual Work Hours)
- **규칙**: 총 근무 시간 - 휴게 시간
- **로직**:
  ```java
  actualWorkHours = totalWorkHours - breakHours;
  ```
- **예시**: 
  - 총 9시간, 휴게 1시간 → 실제 근무 8시간
  - 휴게 없으면 → 실제 근무 = 총 근무 시간

#### 7.4 계산 시점
- **저장 시점**: 기록 생성/수정 시 자동 계산
- **조회 시점**: Response DTO에 포함하여 반환

---

## 🔐 권한 체크

### 직원
- 자신의 출근 기록만 조회 가능
- 자신의 오늘 출근 기록만 휴게/퇴근 가능
- 수정/삭제 불가 (고용주만 가능)

### 고용주
- 자신의 직원의 기록만 조회/수정/삭제 가능
- 모든 직원의 기록 조회 가능 (필터링 가능)
- 언제든지 수정/삭제 가능

---

## 📋 검증 규칙 요약표

| 작업 | 검증 항목 | 규칙 |
|------|----------|------|
| **출근** | 중복 | 같은 날짜 중복 출근 불가 |
| | 소유권 | 자신만 출근 가능 |
| **휴게 시작** | 상태 | IN_PROGRESS만 가능 |
| | 소유권 | 자신의 오늘 기록만 |
| **휴게 끝** | 상태 | ON_BREAK만 가능 |
| | 소유권 | 자신의 오늘 기록만 |
| **퇴근** | 상태 | COMPLETED가 아니어야 함 |
| | 소유권 | 자신의 오늘 기록만 |
| **수정** | 시간 순서 | clockIn < breakStart < breakEnd < clockOut |
| | 소유권 | 자신의 직원만 |
| **삭제** | 소유권 | 자신의 직원만 |

---

## 🔄 상태 전이도

```
[출근]
  ↓
IN_PROGRESS (근무 중)
  ↓ [휴게 시작]
ON_BREAK (휴게 중)
  ↓ [휴게 끝]
IN_PROGRESS (근무 중)
  ↓ [퇴근]
COMPLETED (완료)
```

**상태 변경 규칙**:
- 출근 → IN_PROGRESS
- 휴게 시작 → ON_BREAK
- 휴게 끝 → IN_PROGRESS
- 퇴근 → COMPLETED

---

## ✨ 사용자 경험 (UX)

### 직원 화면
```
[출근하기] 버튼
  ↓ (출근 후)
[휴게시작] [퇴근하기] 버튼
  ↓ (휴게 시작 후)
[휴게끝] 버튼
  ↓ (휴게 끝 후)
[퇴근하기] 버튼
  ↓ (퇴근 후)
"오늘 근무 완료! 총 8시간 근무했습니다."
```

### 고용주 화면
```
[출근 기록 목록]
  - 직원별 필터
  - 날짜별 필터
  - [수정] [삭제] 버튼
  - [수동 등록] 버튼
```

---

## 🚀 구현 계획

1. WorkRecord 엔티티 생성
2. WorkStatus enum 생성
3. WorkRecordRepository 생성
4. DTO 생성 (Request/Response)
5. WorkRecordMapper 생성 (MapStruct)
6. WorkRecordService 생성 (비즈니스 로직)
7. EmployeeWorkRecordController 생성 (직원용)
8. EmployerWorkRecordController 생성 (고용주용)
9. SecurityConfig에 엔드포인트 추가

---

**이 설계로 진행할까요?** 🚀

