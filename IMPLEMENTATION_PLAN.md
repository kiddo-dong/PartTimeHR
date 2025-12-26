# 출근/퇴근 기록 시스템 구현 계획

## 🎯 구현 순서 (단계별)

---

## 1단계: 기본 구조 생성 (도메인 모델)

### 1.1 WorkStatus enum 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/domain/WorkStatus.java`

**내용**:
```java
public enum WorkStatus {
    IN_PROGRESS,  // 근무 중 (출근만 함)
    ON_BREAK,     // 휴게 중 (휴게 시작함)
    COMPLETED      // 근무 완료 (퇴근함)
}
```

**이유**: 상태 관리를 위한 enum이 필요함

---

### 1.2 WorkRecord 엔티티 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/domain/WorkRecord.java`

**내용**:
- Employee와 ManyToOne 관계
- clockInTime, breakStartTime, breakEndTime, clockOutTime 필드
- workDate, status, memo 필드
- @PrePersist, @PreUpdate로 createdAt, updatedAt 자동 설정

**이유**: 출근 기록을 저장할 기본 엔티티

---

## 2단계: 데이터 접근 계층

### 2.1 WorkRecordRepository 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/repository/WorkRecordRepository.java`

**메서드**:
- `findByEmployeeAndWorkDate()` - 특정 직원의 특정 날짜 기록 조회
- `findByEmployee()` - 특정 직원의 모든 기록 조회
- `findByEmployeeAndWorkDateBetween()` - 날짜 범위 조회
- `existsByEmployeeAndWorkDate()` - 중복 출근 체크

**이유**: 데이터베이스 접근을 위한 Repository

---

## 3단계: DTO 생성

### 3.1 Request DTO
**파일들**:
- `CreateWorkRecordRequest.java` - 고용주가 수동 등록할 때
- `UpdateWorkRecordRequest.java` - 고용주가 수정할 때

**내용**:
- Validation 어노테이션 (@NotNull, @Valid 등)
- 필요한 필드만 포함

**이유**: 요청 데이터를 받기 위한 DTO

---

### 3.2 Response DTO
**파일**: `WorkRecordResponse.java`

**내용**:
- 모든 필드 포함
- totalWorkHours, breakHours, actualWorkHours (자동 계산된 값)

**이유**: 응답 데이터를 반환하기 위한 DTO

---

## 4단계: MapStruct Mapper

### 4.1 WorkRecordMapper 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/mapper/WorkRecordMapper.java`

**메서드**:
- `toResponse(WorkRecord)` - Entity → Response DTO
- 근무 시간 계산 로직 포함

**이유**: Entity와 DTO 간 변환

---

## 5단계: 비즈니스 로직 (Service)

### 5.1 WorkRecordService 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/service/WorkRecordService.java`

**메서드들**:

#### 직원용 메서드
1. `clockIn(String employeeEmail)` - 출근하기
   - 오늘 날짜 중복 체크
   - 현재 시간 기록
   - 상태를 IN_PROGRESS로 설정

2. `startBreak(Long recordId, String employeeEmail)` - 휴게 시작
   - 자신의 오늘 기록인지 확인
   - 상태가 IN_PROGRESS인지 확인
   - 현재 시간 기록

3. `endBreak(Long recordId, String employeeEmail)` - 휴게 끝
   - 자신의 오늘 기록인지 확인
   - 상태가 ON_BREAK인지 확인
   - 현재 시간 기록

4. `clockOut(Long recordId, String employeeEmail)` - 퇴근하기
   - 자신의 오늘 기록인지 확인
   - 상태 확인
   - 현재 시간 기록
   - 근무 시간 계산

5. `getMyRecords(String employeeEmail, LocalDate startDate, LocalDate endDate)` - 내 기록 조회
   - 자신의 기록만 조회

6. `getTodayRecord(String employeeEmail)` - 오늘 기록 조회
   - 오늘 날짜의 기록만 조회

#### 고용주용 메서드
7. `createWorkRecord(String employerEmail, CreateWorkRecordRequest)` - 수동 등록
   - 자신의 직원인지 확인
   - 시간 순서 검증
   - 중복 체크

8. `updateWorkRecord(Long recordId, String employerEmail, UpdateWorkRecordRequest)` - 수정
   - 자신의 직원의 기록인지 확인
   - 시간 순서 검증
   - 상태 자동 업데이트
   - 근무 시간 재계산

9. `deleteWorkRecord(Long recordId, String employerEmail)` - 삭제
   - 자신의 직원의 기록인지 확인

10. `getAllRecords(String employerEmail, Long employeeId, LocalDate startDate, LocalDate endDate)` - 전체 조회
    - 자신의 직원들의 기록 조회
    - 필터링 가능

11. `getWorkRecord(Long recordId, String employerEmail)` - 특정 기록 조회
    - 자신의 직원의 기록인지 확인

**이유**: 모든 비즈니스 로직을 Service에서 처리

---

## 6단계: 컨트롤러 (API 엔드포인트)

### 6.1 EmployeeWorkRecordController 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/controller/EmployeeWorkRecordController.java`

**엔드포인트들**:
1. `POST /api/employees/work-records/clock-in` - 출근하기
2. `POST /api/employees/work-records/{id}/break-start` - 휴게 시작
3. `POST /api/employees/work-records/{id}/break-end` - 휴게 끝
4. `POST /api/employees/work-records/{id}/clock-out` - 퇴근하기
5. `GET /api/employees/work-records` - 내 기록 조회
6. `GET /api/employees/work-records/today` - 오늘 기록 조회

**역할**: 직원의 간단한 버튼 클릭 요청 처리

---

### 6.2 EmployerWorkRecordController 생성
**파일**: `src/main/java/com/example/PartTimeHR/workrecord/controller/EmployerWorkRecordController.java`

**엔드포인트들**:
1. `POST /api/employers/work-records` - 수동 등록
2. `PUT /api/employers/work-records/{id}` - 수정
3. `DELETE /api/employers/work-records/{id}` - 삭제
4. `GET /api/employers/work-records` - 전체 조회
5. `GET /api/employers/work-records/{id}` - 특정 기록 조회

**역할**: 고용주의 자유로운 관리 요청 처리

---

## 7단계: Security 설정

### 7.1 SecurityConfig 업데이트
**파일**: `src/main/java/com/example/PartTimeHR/global/config/SecurityConfig.java`

**추가할 내용**:
- 직원 출근/퇴근 엔드포인트는 인증 필요
- 고용주 엔드포인트는 ROLE_EMPLOYER 필요

**이유**: 엔드포인트 보안 설정

---

## 📋 구현 체크리스트

### Phase 1: 기본 구조
- [ ] WorkStatus enum 생성
- [ ] WorkRecord 엔티티 생성
- [ ] WorkRecordRepository 생성

### Phase 2: DTO & Mapper
- [ ] CreateWorkRecordRequest 생성
- [ ] UpdateWorkRecordRequest 생성
- [ ] WorkRecordResponse 생성
- [ ] WorkRecordMapper 생성

### Phase 3: Service (비즈니스 로직)
- [ ] clockIn() - 출근하기
- [ ] startBreak() - 휴게 시작
- [ ] endBreak() - 휴게 끝
- [ ] clockOut() - 퇴근하기
- [ ] getMyRecords() - 내 기록 조회
- [ ] getTodayRecord() - 오늘 기록 조회
- [ ] createWorkRecord() - 수동 등록
- [ ] updateWorkRecord() - 수정
- [ ] deleteWorkRecord() - 삭제
- [ ] getAllRecords() - 전체 조회
- [ ] getWorkRecord() - 특정 기록 조회

### Phase 4: Controller
- [ ] EmployeeWorkRecordController 생성 (6개 엔드포인트)
- [ ] EmployerWorkRecordController 생성 (5개 엔드포인트)

### Phase 5: Security & 테스트
- [ ] SecurityConfig 업데이트
- [ ] Postman 테스트

---

## 🎯 구현 우선순위

### 1순위: 기본 출근/퇴근 기능
1. WorkStatus enum
2. WorkRecord 엔티티
3. WorkRecordRepository
4. 기본 DTO
5. WorkRecordMapper
6. clockIn() - 출근하기
7. clockOut() - 퇴근하기
8. EmployeeWorkRecordController (출근/퇴근만)

**목표**: 직원이 출근하고 퇴근할 수 있는 최소 기능

---

### 2순위: 휴게 기능 추가
9. startBreak() - 휴게 시작
10. endBreak() - 휴게 끝
11. EmployeeWorkRecordController에 휴게 엔드포인트 추가

**목표**: 휴게 기능 완성

---

### 3순위: 고용주 관리 기능
12. createWorkRecord() - 수동 등록
13. updateWorkRecord() - 수정
14. deleteWorkRecord() - 삭제
15. getAllRecords() - 전체 조회
16. getWorkRecord() - 특정 기록 조회
17. EmployerWorkRecordController 생성

**목표**: 고용주가 자유롭게 관리 가능

---

### 4순위: 조회 기능 보완
18. getMyRecords() - 내 기록 조회
19. getTodayRecord() - 오늘 기록 조회

**목표**: 조회 기능 완성

---

## 💡 구현 시 주의사항

### 1. 시간 계산 로직
- LocalDateTime 사용
- Duration.between()으로 시간 차이 계산
- 분 단위로 계산 후 시간으로 변환 (소수점 포함)

### 2. 중복 출근 방지
- 같은 직원 + 같은 날짜 조합 체크
- existsByEmployeeAndWorkDate() 사용

### 3. 상태 관리
- 상태 전이 규칙 준수
- IN_PROGRESS → ON_BREAK → IN_PROGRESS → COMPLETED

### 4. 권한 체크
- 직원: 자신의 기록만 접근
- 고용주: 자신의 직원의 기록만 접근

### 5. 시간 순서 검증
- clockInTime < breakStartTime < breakEndTime < clockOutTime
- 수정 시에도 검증 필요

---

## 🚀 시작할까요?

이 순서대로 하나씩 구현하겠습니다!

