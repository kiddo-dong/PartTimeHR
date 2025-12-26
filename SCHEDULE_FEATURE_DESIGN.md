# 스케줄 관리 기능 설계 문서

## 📋 기능 개요

사장님이 직원들에게 근무 스케줄을 등록하고, 직원들이 자신의 스케줄을 조회할 수 있는 기능입니다.

---

## 🗄️ 데이터베이스 설계

### Schedule 엔티티

```java
@Entity
@Table(name = "schedule")
public class Schedule {
    - id: Long (PK)
    - employee: Employee (ManyToOne) - 어떤 직원의 스케줄인지
    - workDate: LocalDate - 근무 날짜
    - startTime: LocalTime - 시작 시간 (예: 09:00)
    - endTime: LocalTime - 종료 시간 (예: 18:00)
    - status: ScheduleStatus (enum) - 스케줄 상태
        - PENDING: 대기 (등록만 됨)
        - CONFIRMED: 확정 (직원이 확인함)
        - COMPLETED: 완료 (근무 완료)
        - CANCELLED: 취소
    - memo: String - 메모 (선택사항)
    - createdAt: LocalDateTime
    - updatedAt: LocalDateTime
}
```

### 관계
- Schedule : Employee = N : 1 (한 직원은 여러 스케줄을 가질 수 있음)
- Schedule은 Employee를 통해 Employer와 연결됨

---

## 🎯 기능 요구사항

### 1. 사장님 기능 (ROLE_EMPLOYER)

#### 1.1 스케줄 등록
- **엔드포인트**: `POST /api/employers/schedules`
- **권한**: ROLE_EMPLOYER
- **요청 본문**:
  ```json
  {
    "employeeId": 1,
    "workDate": "2025-12-25",
    "startTime": "09:00",
    "endTime": "18:00",
    "memo": "오픈 근무"
  }
  ```
- **검증 규칙**:
  - 자신의 직원인지 확인
  - workDate는 오늘 이후여야 함 (과거 날짜 불가)
  - startTime < endTime
  - 같은 직원의 같은 날짜에 중복 스케줄 불가
- **응답**: `201 Created` + 생성된 스케줄 정보

#### 1.2 스케줄 수정
- **엔드포인트**: `PUT /api/employers/schedules/{scheduleId}`
- **권한**: ROLE_EMPLOYER
- **요청 본문**: 등록과 동일
- **검증 규칙**:
  - 자신의 직원의 스케줄인지 확인
  - COMPLETED 상태는 수정 불가
- **응답**: `200 OK` + 수정된 스케줄 정보

#### 1.3 스케줄 삭제
- **엔드포인트**: `DELETE /api/employers/schedules/{scheduleId}`
- **권한**: ROLE_EMPLOYER
- **검증 규칙**:
  - 자신의 직원의 스케줄인지 확인
  - COMPLETED 상태는 삭제 불가
- **응답**: `204 No Content`

#### 1.4 전체 스케줄 조회 (사장님)
- **엔드포인트**: `GET /api/employers/schedules`
- **권한**: ROLE_EMPLOYER
- **쿼리 파라미터**:
  - `employeeId` (선택): 특정 직원의 스케줄만 조회
  - `startDate` (선택): 시작 날짜 (예: 2025-12-01)
  - `endDate` (선택): 종료 날짜 (예: 2025-12-31)
  - `status` (선택): 스케줄 상태 필터
- **응답**: `200 OK` + 스케줄 목록 (직원 정보 포함)

#### 1.5 특정 스케줄 조회
- **엔드포인트**: `GET /api/employers/schedules/{scheduleId}`
- **권한**: ROLE_EMPLOYER
- **응답**: `200 OK` + 스케줄 상세 정보

---

### 2. 직원 기능 (ROLE_EMPLOYEE)

#### 2.1 내 스케줄 조회
- **엔드포인트**: `GET /api/employees/schedules`
- **권한**: ROLE_EMPLOYEE
- **쿼리 파라미터**:
  - `startDate` (선택): 시작 날짜
  - `endDate` (선택): 종료 날짜
  - `status` (선택): 스케줄 상태 필터
- **응답**: `200 OK` + 자신의 스케줄 목록

#### 2.2 특정 스케줄 조회
- **엔드포인트**: `GET /api/employees/schedules/{scheduleId}`
- **권한**: ROLE_EMPLOYEE
- **검증 규칙**: 자신의 스케줄인지 확인
- **응답**: `200 OK` + 스케줄 상세 정보

#### 2.3 스케줄 확인 (확정)
- **엔드포인트**: `PUT /api/employees/schedules/{scheduleId}/confirm`
- **권한**: ROLE_EMPLOYEE
- **검증 규칙**: 자신의 스케줄이고 PENDING 상태여야 함
- **응답**: `200 OK` + 확정된 스케줄 정보

---

## 📊 API 엔드포인트 요약

### 사장님 (ROLE_EMPLOYER)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/employers/schedules` | 스케줄 등록 | ROLE_EMPLOYER |
| PUT | `/api/employers/schedules/{id}` | 스케줄 수정 | ROLE_EMPLOYER |
| DELETE | `/api/employers/schedules/{id}` | 스케줄 삭제 | ROLE_EMPLOYER |
| GET | `/api/employers/schedules` | 전체 스케줄 조회 | ROLE_EMPLOYER |
| GET | `/api/employers/schedules/{id}` | 특정 스케줄 조회 | ROLE_EMPLOYER |

### 직원 (ROLE_EMPLOYEE)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/employees/schedules` | 내 스케줄 조회 | ROLE_EMPLOYEE |
| GET | `/api/employees/schedules/{id}` | 특정 스케줄 조회 | ROLE_EMPLOYEE |
| PUT | `/api/employees/schedules/{id}/confirm` | 스케줄 확인(확정) | ROLE_EMPLOYEE |

---

## 📝 DTO 설계

### Request DTO

#### CreateScheduleRequest
```java
{
    employeeId: Long (필수)
    workDate: LocalDate (필수) - "2025-12-25"
    startTime: LocalTime (필수) - "09:00"
    endTime: LocalTime (필수) - "18:00"
    memo: String (선택)
}
```

#### UpdateScheduleRequest
```java
{
    workDate: LocalDate (선택)
    startTime: LocalTime (선택)
    endTime: LocalTime (선택)
    memo: String (선택)
}
```

### Response DTO

#### ScheduleResponse
```java
{
    id: Long
    employeeId: Long
    employeeName: String
    workDate: LocalDate
    startTime: LocalTime
    endTime: LocalTime
    status: String (PENDING, CONFIRMED, COMPLETED, CANCELLED)
    memo: String
    workHours: Double (근무 시간, 자동 계산)
    createdAt: LocalDateTime
    updatedAt: LocalDateTime
}
```

---

## 🔍 비즈니스 로직 상세

### 1. 스케줄 등록 시 (CreateSchedule)

#### 1.1 기본 상태 설정
- **규칙**: 새로 등록되는 스케줄의 기본 상태는 항상 `PENDING`
- **이유**: 직원이 확인하기 전까지는 대기 상태로 유지
- **예시**:
  ```
  사장님이 스케줄 등록 → status = PENDING
  직원이 확인 → status = CONFIRMED
  ```

#### 1.2 중복 스케줄 검증
- **규칙**: 같은 직원의 같은 날짜에 중복 스케줄 등록 불가
- **검증 로직**:
  ```java
  // 같은 직원(employeeId) + 같은 날짜(workDate) 조합이 이미 존재하는지 확인
  if (scheduleRepository.existsByEmployeeIdAndWorkDate(employeeId, workDate)) {
      throw new IllegalArgumentException("해당 날짜에 이미 스케줄이 등록되어 있습니다.");
  }
  ```
- **예외 케이스**:
  - ✅ 같은 직원이 다른 날짜에 여러 스케줄 등록 → 가능
  - ❌ 같은 직원이 같은 날짜에 두 번째 스케줄 등록 → 불가
  - ✅ 다른 직원이 같은 날짜에 스케줄 등록 → 가능

#### 1.3 날짜 검증
- **규칙**: 과거 날짜 스케줄 등록 불가
- **검증 로직**:
  ```java
  LocalDate today = LocalDate.now();
  if (workDate.isBefore(today)) {
      throw new IllegalArgumentException("과거 날짜에는 스케줄을 등록할 수 없습니다.");
  }
  ```
- **예외 케이스**:
  - ✅ 오늘 날짜 → 가능
  - ✅ 내일 이후 날짜 → 가능
  - ❌ 어제 이전 날짜 → 불가
- **참고**: 오늘 날짜는 `isBefore()`가 false이므로 등록 가능

#### 1.4 시간 검증
- **규칙**: 시작 시간(startTime)은 종료 시간(endTime)보다 이전이어야 함
- **검증 로직**:
  ```java
  if (!startTime.isBefore(endTime)) {
      throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
  }
  ```
- **예외 케이스**:
  - ✅ 09:00 ~ 18:00 → 가능
  - ✅ 14:00 ~ 22:00 → 가능
  - ❌ 18:00 ~ 09:00 → 불가 (같은 날 기준)
  - **참고**: 자정을 넘어가는 근무(예: 22:00 ~ 06:00)는 현재 설계에서는 불가
    - 필요시 별도 처리 필요 (다음 날로 계산)

#### 1.5 사장님 소유 직원 검증
- **규칙**: 자신의 직원에게만 스케줄 등록 가능
- **검증 로직**:
  ```java
  Employee employee = employeeRepository.findById(employeeId)
      .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
  
  if (!employee.getEmployer().getId().equals(currentEmployerId)) {
      throw new IllegalArgumentException("자신의 직원에게만 스케줄을 등록할 수 있습니다.");
  }
  ```

---

### 2. 스케줄 수정 시 (UpdateSchedule)

#### 2.1 상태 검증
- **규칙**: `COMPLETED` 상태의 스케줄은 수정 불가
- **검증 로직**:
  ```java
  if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
      throw new IllegalArgumentException("완료된 스케줄은 수정할 수 없습니다.");
  }
  ```
- **이유**: 이미 근무가 완료된 스케줄은 변경하면 안 됨 (데이터 무결성)
- **수정 가능한 상태**:
  - ✅ PENDING → 수정 가능
  - ✅ CONFIRMED → 수정 가능
  - ❌ COMPLETED → 수정 불가
  - ❌ CANCELLED → 수정 불가 (취소된 스케줄도 수정 불가)

#### 2.2 중복 검증
- **규칙**: 수정 후에도 중복 스케줄 체크 필요
- **검증 로직**:
  ```java
  // 현재 수정 중인 스케줄을 제외하고 중복 체크
  if (scheduleRepository.existsByEmployeeIdAndWorkDateAndIdNot(
          employeeId, workDate, scheduleId)) {
      throw new IllegalArgumentException("해당 날짜에 이미 다른 스케줄이 등록되어 있습니다.");
  }
  ```
- **예시**:
  ```
  직원 A의 2025-12-25 스케줄이 이미 존재
  → 같은 날짜로 수정 시도 → 에러 발생
  ```

#### 2.3 날짜/시간 검증
- **규칙**: 등록 시와 동일한 검증 규칙 적용
- **검증 항목**:
  - 과거 날짜 불가
  - startTime < endTime
  - (날짜 변경 시) 중복 체크

#### 2.4 사장님 소유 검증
- **규칙**: 자신의 직원의 스케줄만 수정 가능
- **검증 로직**: 등록 시와 동일

---

### 3. 스케줄 삭제 시 (DeleteSchedule)

#### 3.1 상태 검증
- **규칙**: `COMPLETED` 상태의 스케줄은 삭제 불가
- **검증 로직**:
  ```java
  if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
      throw new IllegalArgumentException("완료된 스케줄은 삭제할 수 없습니다.");
  }
  ```
- **이유**: 완료된 근무 기록은 삭제하면 안 됨 (급여 계산 등에 사용)
- **삭제 가능한 상태**:
  - ✅ PENDING → 삭제 가능
  - ✅ CONFIRMED → 삭제 가능 (직원이 확인했어도 취소 가능)
  - ❌ COMPLETED → 삭제 불가
  - ✅ CANCELLED → 삭제 가능 (이미 취소된 건은 삭제 가능)

#### 3.2 소프트 삭제 vs 하드 삭제
- **현재 설계**: 하드 삭제 (DB에서 완전히 제거)
- **대안**: 소프트 삭제 (deletedAt 필드 추가, status를 CANCELLED로 변경)
- **권장**: 소프트 삭제가 더 안전 (이력 관리)

#### 3.3 사장님 소유 검증
- **규칙**: 자신의 직원의 스케줄만 삭제 가능
- **검증 로직**: 등록 시와 동일

---

### 4. 스케줄 확인 시 (ConfirmSchedule - 직원)

#### 4.1 상태 변경
- **규칙**: `PENDING` → `CONFIRMED` 상태로 변경
- **로직**:
  ```java
  if (schedule.getStatus() != ScheduleStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 스케줄만 확인할 수 있습니다.");
  }
  schedule.setStatus(ScheduleStatus.CONFIRMED);
  ```
- **상태 흐름**:
  ```
  PENDING (등록됨) 
    ↓ [직원 확인]
  CONFIRMED (확정됨)
    ↓ [근무 완료]
  COMPLETED (완료됨)
  ```

#### 4.2 소유권 검증
- **규칙**: 자신의 스케줄만 확인 가능
- **검증 로직**:
  ```java
  Employee currentEmployee = getCurrentEmployee(); // JWT에서 추출
  if (!schedule.getEmployee().getId().equals(currentEmployee.getId())) {
      throw new IllegalArgumentException("자신의 스케줄만 확인할 수 있습니다.");
  }
  ```

#### 4.3 중복 확인 방지
- **규칙**: 이미 CONFIRMED 상태인 스케줄은 다시 확인 불가
- **검증**: 상태 검증에서 이미 처리됨

---

### 5. 근무 시간 계산

#### 5.1 기본 계산 로직
- **규칙**: 종료 시간 - 시작 시간 = 근무 시간
- **로직**:
  ```java
  Duration duration = Duration.between(startTime, endTime);
  double workHours = duration.toHours(); // 정수 시간
  // 또는
  double workHours = duration.toMinutes() / 60.0; // 소수점 포함 (예: 8.5시간)
  ```
- **예시**:
  ```
  startTime: 09:00
  endTime: 18:00
  → workHours = 9.0 시간
  
  startTime: 09:00
  endTime: 17:30
  → workHours = 8.5 시간
  ```

#### 5.2 계산 시점
- **저장 시점**: 스케줄 등록/수정 시 자동 계산하여 저장
- **조회 시점**: Response DTO에 포함하여 반환
- **로직 위치**: Service 레이어에서 계산

#### 5.3 추가 고려사항 (향후 확장)
- **점심시간 제외**: 
  - 예: 09:00 ~ 18:00 (1시간 점심 제외) = 8시간
  - 현재는 미구현, 필요시 추가
- **야간 수당**: 
  - 예: 22:00 이후 근무 시 추가 수당
  - 현재는 미구현, 필요시 추가
- **자정 넘어가는 근무**:
  - 예: 22:00 ~ 06:00 (다음 날)
  - 현재는 미구현, 필요시 별도 처리

---

## 📋 검증 규칙 요약표

| 작업 | 검증 항목 | 규칙 |
|------|----------|------|
| **등록** | 날짜 | 과거 날짜 불가 |
| | 시간 | startTime < endTime |
| | 중복 | 같은 직원 + 같은 날짜 불가 |
| | 소유권 | 자신의 직원만 |
| **수정** | 상태 | COMPLETED 불가 |
| | 중복 | 수정 후에도 중복 체크 |
| | 날짜/시간 | 등록과 동일한 검증 |
| | 소유권 | 자신의 직원만 |
| **삭제** | 상태 | COMPLETED 불가 |
| | 소유권 | 자신의 직원만 |
| **확인** | 상태 | PENDING만 가능 |
| | 소유권 | 자신의 스케줄만 |

---

## 🔄 상태 전이도

```
[등록]
  ↓
PENDING (대기)
  ↓ [직원 확인]
CONFIRMED (확정)
  ↓ [근무 완료]
COMPLETED (완료)

[취소]
  ↓
CANCELLED (취소)
```

**상태 변경 규칙**:
- PENDING → CONFIRMED: 직원이 확인
- CONFIRMED → COMPLETED: (향후 구현) 근무 완료 처리
- PENDING/CONFIRMED → CANCELLED: 사장님이 취소
- COMPLETED: 수정/삭제 불가 (최종 상태)

---

## 🔐 권한 체크

### 사장님
- 자신의 직원의 스케줄만 조회/수정/삭제 가능
- 모든 직원의 스케줄 조회 가능 (필터링 가능)

### 직원
- 자신의 스케줄만 조회 가능
- 자신의 스케줄만 확인(확정) 가능

---

## ❓ 확인이 필요한 사항

1. **스케줄 상태 관리**
   - PENDING → CONFIRMED → COMPLETED 흐름이 맞나요?
   - COMPLETED는 언제 자동으로 변경되나요? (수동? 자동?)

2. **스케줄 수정/삭제 권한**
   - 직원이 자신의 스케줄을 수정/삭제할 수 있나요?
   - 아니면 사장님만 수정/삭제 가능한가요?

3. **과거 날짜 스케줄**
   - 과거 날짜 스케줄 등록이 필요한 경우가 있나요?
   - (예: 지난 주 스케줄을 나중에 등록하는 경우)

4. **스케줄 중복**
   - 같은 직원이 같은 날짜에 여러 스케줄을 가질 수 있나요?
   - (예: 오전 근무 + 오후 근무)

5. **근무 시간 계산**
   - 점심시간 제외가 필요한가요?
   - 야간 수당 등 추가 계산이 필요한가요?

---

## ✅ 기본 구현 계획

1. Schedule 엔티티 생성
2. ScheduleStatus enum 생성
3. ScheduleRepository 생성
4. DTO 생성 (Request/Response)
5. ScheduleMapper 생성 (MapStruct)
6. ScheduleService 생성 (비즈니스 로직)
7. ScheduleController 생성 (사장님용)
8. EmployeeScheduleController 생성 (직원용)
9. SecurityConfig에 엔드포인트 추가

---

**확인 후 진행하겠습니다!** 🚀

