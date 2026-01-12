# PartTimeHR 프로젝트

## 1. 프로젝트 개요

**프로젝트명**: PartTimeHR

**목적**: 사장님(자영업자)을 위한 알바 관리 웹 애플리케이션

---

## 2. 기술 스택
### Backend
- **Spring Boot**: 4.0.0
- **Java**: 21
- **Spring Security**: 7.0.0
- **Spring Data JPA**: 4.0.0

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
---

## 3. 프로젝트 구조

```
PartTimeHR/
├── global/             # 공통 기능
│   ├── config/         # SecurityConfig
│   ├── dto/         
│   └── exception/      # 예외
├── security/           # 보안 설정
│   ├── customuser/     # CustomUserDetails
│   ├── handler/        # FormLogin Handler
│   └── jwt/            # JWT 인증관련
├── mail/               # SMTP(이메일) 인증 관련
│   ├── domain/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── exception/
├── employer/           # 사장님 관련
│   ├── domain/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── mapper/
├── employee/           # 직원 관련
│   ├── domain/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── mapper/
├── paypolicy/          # 사장님 정책 관련
│   ├── domain/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
├── workrecord/         # 근태 관리
│   ├── domain/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   ├── mapper/
│   └── exception/
└── statistics/         # 통계 관리
    ├── controller/
    ├── service/
    └── dto/
```

---

## 4. 보안 기능

### 사용자(Employer) 생성 시 email 인증 (SMTP)
- Employer의 계정 생성 시 email 인증 발송
- Email 인증이 되지않은 계정은 Login 불가

### JWT 인증 시스템
- JWT 토큰 생성 (`JwtTokenProvider`)
- JWT 토큰 검증 (`JwtAuthenticationFilter`)
- SecurityContext에 인증 정보 저장
- 토큰 만료 시간: 24시간 (추 후 refrashToken으로 확장)

### 역할 기반 API 접근 제어
- `ROLE_EMPLOYER` - 사장님 권한
- `ROLE_EMPLOYEE` - 직원 권한
- `ROLE_ADMIN` - 관리자 권한 (추 후 사용)
- `@PreAuthorize` 어노테이션으로 엔드포인트 보호

### 비밀번호 보안
- BCrypt 암호화(필요 시 argon2 확장)

---

## 5. 기능

### 1. Employer (사장님) 기능
#### 기본 기능
- 조회
- 수정
- 제거

#### 인증/인가
- 회원가입
- 비밀번호 찾기
- 비밀번호 리셋
- 로그인 - JWT 토큰 발급

#### 직원 관리
- 직원 등록 - 직원은 스스로 계정 생성이 불가능 -> 사장님이 등록해줘야 계정(Employee) 사용 가능
- 직원 정보 수정/제거
- 직원 목록 조회 (단일/전체/조건별)
- 직원 근태 관리 - 출근/휴게/퇴근(생성/조회/수정/삭제 가능)

#### 매장 관리
- 주간별 통계 (직원) -> 추후 주휴 수당 개선
- 월별 통계 (직원) -> 추후 개선 예정

### 2. Employee (직원) 기능
#### 기본 기능
- 조회

#### 인증/인가
- 로그인 - JWT 토큰 발급

---

## 6. 아키텍처 패턴

### 레이어드 아키텍처
```
Controller (표현 계층)
    ↓
Service (비즈니스 계층)
    ↓
Repository (데이터 접근 계층) - jpa 사용
    ↓
Database - MySql 8.0
```

### 역할 분담
- **Controller**: HTTP 요청/응답 처리, 인증 정보 추출
- **Service**: 비즈니스 로직, Entity ↔ DTO 변환 (MapStruct)
- **Repository**: 데이터베이스 접근
- **DTO** : 데이터 안전 접근
- **Mapper**: Entity와 DTO 간 변환 (MapStruct)
---

## 7. 주요 엔티티

### Employer (사장님)
```
- id: Long (PK)
- email: String (unique, 로그인용)
- password: String (암호화)
- emailVerified: boolean (이메일 인증 확인용)
- role: Role (ROLE_EMPLOYER)
- name: String
- phone: String
- storeName: String (가게명)
- weekStartDay: Integer (주간 시작 요일)
- employees: List<Employee> (피고용주)
- weeklyPayApplicable: boolean (주휴수당 지급 여부)
- createdAt: LocalDateTime 
- updatedAt: LocalDateTime
```

### Employee (직원)
```
- id: Long (PK)
- email: String (unique, 로그인용)
- password: String (암호화)
- role: Role (ROLE_EMPLOYEE)
- name: String
- phone: String
- employer: Employer (고용주 참조 | ManyToOne)
- PayPolicy: PayPolicy (직급/시급 | ManyToOne)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

---

## 8. API 명세

### Employer APIs

| Method | Endpoint | 설명 | 인증 | 권한 |
|--------|----------|------|------|------|
| POST | `/api/employers/signup` | 회원가입 | ❌ | - |
| POST | `/api/employers/login` | 로그인 | ❌ | - |
| GET | `/api/employers/me` | 내 정보 조회 | ✅ | - |
| GET | `/api/employers/profile` | 프로필 조회 | ✅ | - |
| GET | `/api/employers/dashboard` | 대시보드 | ✅ | ROLE_EMPLOYER |
| POST | `/api/employers/employees` | 직원 등록 | ✅ | ROLE_EMPLOYER |
| GET | `/api/employers/employees` | 직원 목록 | ✅ | ROLE_EMPLOYER |

### Employee APIs

| Method | Endpoint | 설명 | 인증 | 권한 |
|--------|----------|------|------|------|
| POST | `/api/employees/signup` | 회원가입 | ❌ | - |
| POST | `/api/employees/login` | 로그인 | ❌ | - |
| GET | `/api/employees/me` | 내 정보 조회 | ✅ | - |
| GET | `/api/employees/dashboard` | 대시보드 | ✅ | ROLE_EMPLOYEE |

---

## 9. 주요 특징

### 1. Security 설정
- CSRF 비활성화 (JWT 사용)
- Stateless 세션 (JWT 기반)
- `@PreAuthorize` 어노테이션
- SecurityConfig에서 URL 기반 접근 제어
- JWT 토큰에 역할 정보 포함
- `/api/employers/signup`, `/api/employers/login` - 인증 불필요
- `/api/employees/signup`, `/api/employees/login` - 인증 불필요
- 나머지 엔드포인트 - 인증 필요

### 2. MapStruct 활용
- Entity ↔ DTO 자동 변환
- 컴파일 타임 코드 생성 (런타임 오버헤드 없음)
- 타입 안전성 보장

### 3. 전역 예외 처리
- `GlobalExceptionHandler`로 일관된 에러 응답
- Validation 에러 처리
- 비즈니스 로직 에러 처리

---

## 10. 데이터베이스 스키마

### employer 테이블
```sql
CREATE TABLE employer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_EMPLOYER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN') NOT NULL,
    employer_name VARCHAR(30) NOT NULL,
    employer_phone VARCHAR(20) NOT NULL,
    store_name VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);
```

### employee 테이블
```sql
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_EMPLOYER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN') NOT NULL,
    employee_name VARCHAR(30) NOT NULL,
    employee_phone VARCHAR(20) NOT NULL,
    employer_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    FOREIGN KEY (employer_id) REFERENCES employer(id)
);
```

---

## 11. 테스트 상태

### 완료된 테스트
- ✅ Employer 회원가입/로그인
- ✅ JWT 토큰 발급 및 검증
- ✅ 인증된 엔드포인트 접근
- ✅ 역할 기반 접근 제어
- ✅ Employee 회원가입/로그인
- ✅ 사장님이 직원 등록
- ✅ 사장님이 직원 목록 조회

### 테스트 도구
- Spring Test
- Postman
- JWT.io (토큰 검증)
---

## 12. 추후 추가 기능

### 추가 기능
1. 스케줄 추가 기능
   - 간편성 고려
   
2. 알림 기능
   - 스케줄 변경 알림
   - 급여 지급 알림
   
### 개선 사항
1. Refresh Token 구현
2. 비밀번호 변경 기능
3. 로그아웃 기능 (토큰 블랙리스트)
4. 이메일 인증
5. 프로필 수정 기능

---

## 13. 개발 이슈

1. Contoller기반 Login -> SpringSecurity FormLogin 변경
2. lombok & MapStruct 병행 사용으로 인한 Runtime Error -> 의존성 추가
3. JPA의 DB조회 비용 증가 -> 
---

## 14. 중요 포인트

1. **Spring Security + JWT**: Stateless 인증 구현
2. **SMTP 전송 구현**: Employer 생성 시 email 인증(보안 강화)
2. **MapStruct**: Entity ↔ DTO 자동 변환
3. **레이어드 아키텍처**: 역할 분담 구조화
4. **역할 기반 접근 제어**: Role 별 API 접근 제한
5. **전역 예외 처리**: 일관된 에러 응답

---

**작성자**: 최동현
**작성일**: 2025-12-24  
**수정일**: 2026-01-12
**프로젝트 상태**: 진행중