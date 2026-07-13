# PartTimeHR 프로젝트(초기 MVP)

## 1. 프로젝트 개요

**프로젝트명**: al-bam(알밤)

**목적**: 사장님(자영업자)을 위한 직원/매장 관리 웹 애플리케이션

---

## 2. 기술 스택
### Backend
- **Spring Boot**: 4.0.0
- **Java**: 21
- **Spring Security**: 7.0.0
- **Spring Data JPA**: 4.0.0 (Native SQL 병행)
- **Spring Ai** : (추 후 예정)

### Database
- **MySQL**: 8.0.44
- **Hibernate**: 7.1.8.Final
- **JPA**: Jakarta Persistence 3.2.0

### Authentication/Security
- **JWT**: jjwt 0.13.0
- **SMTP**: Spring-mail
- **BCrypt**: Spring Security 내장

### Util
- **Lombok**: 1.18.42
- **MapStruct**: 1.6.3 (Entity ↔ DTO 변환)
- **Validation**: Jakarta Validation 3.1.1

### AI
- **OpenAI(Chat GPT)** : gpt-4o
---

## 3. 프로젝트 구조

도메인별로 패키지를 나누고, 각 도메인 안을 **presentation / application / domain** 3계층으로 구성한다.

| 계층 | 담는 것 | 의존 방향 |
|------|---------|-----------|
| `presentation` | Controller, Request/Response DTO | → application |
| `application` | Service, Mapper (유스케이스, 트랜잭션 경계) | → domain |
| `domain` | Entity, Repository, 도메인 예외 | 의존 없음 |

```
PartTimeHR/
├── auth/               # 통합 로그인 (Employer/Employee 공용)
│   ├── presentation/   # AuthController + dto
│   ├── application/    # AuthService
│   └── domain/         # AuthPrincipal, InvalidCredentialsException
├── employer/           # 사장님
│   ├── presentation/   # EmployerController, EmployerAuthController + dto
│   ├── application/    # EmployerService, EmployerAuthService, EmployerMapper
│   └── domain/         # Employer, Role, EmployerRepository, 예외
├── store/              # 매장(가게)
├── employee/           # 직원
├── paypolicy/          # 직급/시급 정책
├── schedule/           # 근무 스케줄 (예정)
├── workrecord/         # 근태 기록 (실제 출퇴근)
├── attendance/         # 스케줄 ↔ 근태 대조 통계
├── mail/               # SMTP 이메일 인증 / 비밀번호 재설정 토큰
│   (위 도메인 모두 presentation / application / domain 3계층 동일)
│
├── security/           # 보안 (도메인 아님)
│   ├── customuser/     # CustomUserDetails
│   ├── handler/        # Login Handler
│   └── jwt/            # JwtProvider, JwtAuthenticationFilter
├── global/             # 공통
│   ├── config/         # SecurityConfig
│   ├── dto/            # ErrorResponse
│   └── exception/      # GlobalExceptionHandler
└── common/
    └── presentation/   # RootController (헬스체크)
```

---

## 4. 보안 기능

### 사용자(Employer) 생성 시 email 인증 (SMTP)
- Employer의 계정 생성 시 email 인증 발송
- Email 인증이 되지 않은 계정은 로그인 불가 (403 `EMAIL_NOT_VERIFIED`)
- 인증/재설정 메일 재발송은 이메일당 60초 쿨다운 (429)

### JWT 인증 시스템
- Access Token 24시간 + Refresh Token 14일 (DB 저장, 계정당 1개)
- `POST /api/refresh`로 access 토큰 재발급, `POST /api/logout`으로 refresh 폐기
- 필터에서 인증 실패 시 예외 대신 401 JSON 응답 (스택트레이스 미노출)

### 역할 기반 API 접근 제어
- `ROLE_EMPLOYER` - 사장님 권한
- `ROLE_EMPLOYEE` - 직원 권한
- `ROLE_ADMIN` - 관리자 권한 (추후 사용)
- `@PreAuthorize` 어노테이션으로 엔드포인트 보호

### 데이터 접근 검증
- 매장 하위 리소스(직원/정책/스케줄/근무기록)는 모두 매장 소유권 + 소속 검증을 거침
- 이메일은 사장·직원 통틀어 전역 유일 (로그인 충돌 방지)

### 비밀번호 보안
- BCrypt 암호화 (수정/재설정 포함 모든 경로에서 암호화 저장)
- 비밀번호 재설정 토큰은 30분 유효, 1회용

---

## 5. 환경 변수

시크릿은 코드에 커밋하지 않고 환경변수로 주입한다.

| 변수 | 용도 | 기본값 (로컬 개발용) |
|------|------|---------------------|
| `DB_URL` | MySQL 접속 URL | `jdbc:mysql://localhost:3306/parttime_hr` |
| `DB_USERNAME` / `DB_PASSWORD` | DB 계정 | `root` / `password` |
| `JWT_SECRET` | JWT 서명 키 (운영 필수 교체) | 개발용 기본값 |
| `JWT_EXPIRATION` | access 토큰 만료(ms) | `86400000` (24h) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP 계정/앱 비밀번호 | **MAIL_PASSWORD는 기본값 없음 (필수)** |
| `APP_BASE_URL` | 메일 링크 기준 URL | `http://localhost:8080` |

---

## 6. 기능

### 1. Employer (사장님)
#### 인증/계정
- 회원가입 (첫 매장 + 기본 시급정책 자동 생성, 인증 메일 발송)
- 로그인 (JWT), 토큰 재발급, 로그아웃
- 비밀번호 찾기/재설정 (메일 링크, 1회용 토큰)
- 내 정보 조회/수정

#### 매장 관리
- 매장 생성/조회/수정/삭제 (다매장 1:N, 삭제는 소속 직원이 없을 때만)
- 매장별 시급 정책(직급/시급) 생성/조회/수정/삭제

#### 직원 관리
- 직원 등록 (직원은 자가 가입 불가, 시급 정책 지정 또는 기본 정책)
- 직원 조회(전체/단일)/수정/삭제

#### 스케줄 관리
- 생성(시간 겹침 검증, 하루 여러 타임 가능)/수정/삭제
- 조회: 매장 전체/직원별 × 단일일/기간/주간/월간 (주 시작 요일은 매장 설정)

#### 근태 관리
- 원클릭 출근/휴게 시작/휴게 종료/퇴근 (휴게 여러 번 가능, 누적 집계)
- 수동 생성/수정(부분 수정)/삭제
- 기록 당시 시급/직급 스냅샷 저장

#### 근태 통계
- 일별: 스케줄 vs 실제 출퇴근 대조 (WORKED/LATE/EARLY_LEAVE/PARTIAL/ABSENT/UNSCHEDULED)
- 기간 요약: 출근율(%), 지각/결근 집계

### 2. Employee (직원)
- 로그인 (JWT), 토큰 재발급, 로그아웃
- 내 정보 조회 (소속 매장/직급/시급 포함)
- 본인 스케줄 조회 (당일/기간/주간/월간)
- 본인 출근/휴게/퇴근 + 당일 근무 기록 조회

---

## 7. 아키텍처 패턴

### 도메인별 3계층 (Layered Architecture)
```
presentation (Controller, DTO)
    ↓
application (Service, Mapper - 유스케이스, 트랜잭션 경계)
    ↓
domain (Entity, Repository, 도메인 예외)
    ↓
Database - MySQL 8.0
```

### 역할 분담
- **Controller**: HTTP 요청/응답 처리, 인증 정보 추출 (Repository 직접 접근 금지)
- **Service**: 비즈니스 로직, 접근 권한 검증, Entity ↔ DTO 변환 (MapStruct)
- **Repository**: 데이터베이스 접근 (N+1 방지: @EntityGraph / fetch join)
- **Entity**: setter 대신 의도가 드러나는 도메인 메서드 (updateBasicInfo, changePassword, clockOut 등)

---

## 8. 주요 엔티티

### Employer (사장님)
```
- id, email(전역 unique), password(BCrypt), emailVerified, role
- name, phone, stores(1:N), createdAt/updatedAt
```

### Store (매장)
```
- id, name, phone, address
- weekStartDay(주 시작 요일), weeklyAllowanceIncluded(주휴 포함 시급 계약 여부),
  fiveOrMoreEmployees(상시 5인 이상 여부)
- employer(N:1), employees(1:N), createdAt/updatedAt
```

### Employee (직원)
```
- id, email(전역 unique), password(BCrypt), name, phone
- store(N:1), payPolicy(N:1), role, createdAt/updatedAt
```

### PayPolicy (직급/시급 정책)
```
- id, store(N:1), jobTitle, hourlyWage, isDefault, active, createdAt
```

### Schedule (근무 예정)
```
- id, store(N:1), employee(N:1), workDate
- startTime/endTime, confirmed, createdAt/updatedAt
```

### WorkRecord (근태 기록)
```
- id, employee(N:1), workDate, clockInTime/clockOutTime
- breakStartTime/breakEndTime(마지막 휴게), totalBreakMinutes(누적)
- totalWorkedMinutes/netWorkedMinutes(퇴근 시 확정)
- appliedHourlyWage/appliedJobTitle(당시 정책 스냅샷)
- status(IN_PROGRESS/ON_BREAK/COMPLETED/ABSENT), memo
```

### RefreshToken / EmailVerification / PasswordResetToken
```
- UUID 토큰 + 만료 시각 (각 14일 / 30분 / 30분)
```

---

## 9. API 명세

### 인증 (공개)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/login` | 통합 로그인 (access + refresh 토큰 발급) |
| POST | `/api/refresh` | access 토큰 재발급 |
| POST | `/api/logout` | refresh 토큰 폐기 |
| POST | `/api/employers/signup` | 사장 회원가입 |
| GET | `/api/email/verify?token=` | 이메일 인증 |
| POST | `/api/email/resend?email=` | 인증 메일 재발송 (60초 쿨다운) |
| POST | `/api/employers/password/reset-request` | 비밀번호 재설정 메일 |
| POST | `/api/employers/password/reset` | 비밀번호 재설정 |

### 사장님 (ROLE_EMPLOYER)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/employers/me` | 내 정보 조회 |
| PUT | `/api/employers` | 내 정보 수정 |
| POST / GET | `/api/stores` | 매장 생성 / 내 매장 전체 조회 |
| GET / PUT / DELETE | `/api/stores/{storeId}` | 매장 조회 / 수정 / 삭제 |
| GET / POST | `/api/stores/{storeId}/paypolicies` | 시급 정책 목록 / 생성 |
| PUT / DELETE | `/api/stores/{storeId}/paypolicies/{id}` | 정책 수정 / 삭제 |
| POST | `/api/stores/{storeId}/employees` | 직원 등록 |
| GET | `/api/stores/{storeId}/employees/all` | 직원 전체 조회 |
| GET / PUT / DELETE | `/api/stores/{storeId}/employees/{id}` | 직원 조회 / 수정 / 삭제 |
| POST | `/api/stores/{storeId}/schedules` | 스케줄 생성 |
| PUT / DELETE | `/api/stores/{storeId}/schedules/{id}/employees/{employeeId}` | 스케줄 수정 / 삭제 |
| GET | `/api/stores/{storeId}/schedules/date·period·week·month` | 매장 전체 스케줄 조회 |
| GET | `/api/stores/{storeId}/schedules/employees/{id}/date·period·week·month` | 직원별 스케줄 조회 |
| POST | `/api/stores/{storeId}/work-records/employees/{id}/clock-in·break-start·break-end·clock-out` | 원클릭 근태 |
| POST | `/api/stores/{storeId}/work-records/employees/manual` | 근무 기록 수동 생성 |
| PUT / DELETE | `/api/stores/{storeId}/work-records/{id}` | 근무 기록 수정 / 삭제 |
| GET | `/api/stores/{storeId}/attendance/daily?date=` | 일별 근태 통계 |
| GET | `/api/stores/{storeId}/attendance/summary?from=&to=` | 기간 근태 요약 (출근율) |
| GET | `/api/stores/{storeId}/payroll?from=&to=` | 매장 전체 급여 요약 |
| GET | `/api/stores/{storeId}/payroll/employees/{id}?from=&to=` | 직원별 급여 상세 (기록별 내역) |

### 직원 (ROLE_EMPLOYEE)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/employee/me` | 내 정보 조회 |
| GET | `/api/employee/schedules/today·period·week·month` | 본인 스케줄 조회 |
| POST | `/api/employee/work-records/clock-in·break-start·break-end·clock-out` | 본인 출근/휴게/퇴근 |
| GET | `/api/employee/work-records/today` | 본인 당일 근무 기록 |
| GET | `/api/employee/payroll?from=&to=` | 본인 급여 조회 |

### 급여 계산 규칙 (근로기준법 기준)

- **기본급**: 퇴근 완료된 기록별 `실근무 분 × 당시 시급 스냅샷` (원 단위 반올림)
- **주휴수당** (제55조, 사업장 규모 무관·**지급 의무**): 한 주(매장의 주 시작 요일 기준) 실근무 15시간 이상 + **개근**
  (스케줄이 있는 날마다 근무 기록 존재) 시 `min(주 실근무, 40) / 40 × 8시간 × 그 주 평균 시급`.
  스케줄을 쓰지 않는 매장은 시간 기준만 적용
  - 지급 여부는 선택이 아니며, **지급 방식**만 선택한다: `weeklyAllowanceIncluded=true`("주휴 포함 시급" 계약)
    매장은 별도 계산하지 않는 대신 **시급이 최저임금 × 1.2 이상**이어야 등록/수정 가능.
    급여 응답에 `weeklyAllowanceIncluded`로 방식이 표시됨
- **연장근로 가산** (제56조, **상시 5인 이상 사업장만**): 1일 8시간/주 40시간 초과분 × 시급 × 50%
- **야간근로 가산** (제56조, 상시 5인 이상만): 22:00~06:00 실근로 분(휴게 제외) × 시급 × 50%
- **최저임금**: 시급 정책 생성/수정 시 최저시급(`MINIMUM_WAGE`, 기본 10,320원) 미달 차단
- 단순화: 조회 구간이 주 중간을 자르면 그 주는 잘린 부분만 집계 (정산은 주 단위로 조회 권장),
  휴일근로 가산 미구현. 휴게 부여 의무(제54조, 4시간당 30분)는 기록 기반 시스템 특성상 강제하지 않음
- **미퇴근 자동 마감**: 퇴근을 찍지 않은 채 다음 출근을 하면 이전 근무는
  근무일 23:59 기준으로 자동 마감되고 메모에 `[미퇴근 자동 마감]` 표시 (사장이 수동 수정 가능)

### 에러 응답 규약

```json
{ "code": "ACCESS_DENIED", "message": "가게 접근 권한이 없습니다.", "timestamp": "..." }
```

| 상태 | 코드 | 상황 |
|------|------|------|
| 401 | INVALID_CREDENTIALS | 로그인/refresh 실패 |
| 403 | ACCESS_DENIED, EMAIL_NOT_VERIFIED | 권한/소유권 위반, 미인증 로그인 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT, INVALID_STATE | 중복(이메일/스케줄), 상태 충돌(진행 중 근무 등) |
| 429 | TOO_MANY_REQUESTS | 메일 재발송 쿨다운 |
| 400 | INVALID_ARGUMENT, VALIDATION_FAILED | 잘못된 요청 |

---

## 10. 테스트

- `WorkRecordTest`: 근태 상태 머신 (다회 휴게, 집계 확정, 자동 마감, 상태 위반)
- `PayrollCalculatorTest`: 급여 계산 (기본급, 주휴수당 판정, 주별 분리)
- `AttendanceServiceTest`: 근태 통계 (결근/지각/예정 판정, 기간 집계, 범위 검증)
- 실행: `./mvnw test`

---

## 11. 추후 추가 기능

1. 알림 기능 (스케줄 변경, 급여 지급)
2. 직급별 스케줄 조회
3. 직원 soft delete (급여 이력 보존)
4. access 토큰 블랙리스트 (현재 로그아웃은 refresh만 폐기)
5. 통합 테스트 확충 (Testcontainers)
6. 급여 계산 고도화 (개근 판정, 야간/연장 수당, 세금/보험 공제)

---

## 12. 중요 포인트

1. **도메인별 3계층**: presentation → application → domain 단방향 의존
2. **접근 검증 일원화**: StoreAccessService/EmployeeAccessService로 소유권·소속 검증
3. **시급 스냅샷**: 근태 기록에 당시 시급/직급 저장 → 정책 변경에도 급여 이력 안전
4. **Stateless JWT + Refresh Token**: DB 저장 refresh로 로그아웃/재발급 지원
5. **전역 예외 처리**: 커스텀 예외 → 일관된 상태 코드/JSON 매핑
6. **시크릿 외부화**: 환경변수 주입 (기본값은 로컬 개발용)

---

**작성자**: 최동현
**작성일**: 2025-12-24
**수정일**: 2026-07-13
**프로젝트 상태**: 진행중
