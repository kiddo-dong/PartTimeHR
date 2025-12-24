# PartTimeHR 프로젝트 요약

## 📋 프로젝트 개요

**프로젝트명**: PartTimeHR  
**목적**: 자영업자(사장님)를 위한 알바 관리 웹 애플리케이션  
**기술 스택**: Spring Boot 4.0.0, Java 21, MySQL, JWT, MapStruct

---

## 🏗️ 프로젝트 구조

```
PartTimeHR/
├── employee/          # 직원 관련
│   ├── controller/   # EmployeeController
│   ├── domain/       # Employee 엔티티
│   ├── dto/          # Request/Response DTO
│   ├── mapper/       # MapStruct Mapper
│   ├── repository/   # EmployeeRepository
│   └── service/      # EmployeeService
├── employer/         # 사장님 관련
│   ├── controller/   # EmployerController, EmployerLoginController
│   ├── domain/       # Employer 엔티티, Role enum
│   ├── dto/          # Request/Response DTO
│   ├── mapper/       # MapStruct Mapper
│   ├── repository/   # EmployerRepository
│   └── service/      # EmployerService
└── global/           # 공통 기능
    ├── config/       # SecurityConfig
    ├── exception/    # GlobalExceptionHandler
    └── jwt/          # JWT 인증 관련
```

---

## 🎯 구현된 기능

### 1. Employer (사장님) 기능

#### 인증/인가
- ✅ 회원가입 (`POST /api/employers/signup`)
- ✅ 로그인 (`POST /api/employers/login`) - JWT 토큰 발급
- ✅ 내 정보 조회 (`GET /api/employers/me`)
- ✅ 프로필 조회 (`GET /api/employers/profile`)
- ✅ 사장님 전용 대시보드 (`GET /api/employers/dashboard`)

#### 직원 관리
- ✅ 직원 등록 (`POST /api/employers/employees`)
- ✅ 직원 목록 조회 (`GET /api/employers/employees`)

### 2. Employee (직원) 기능

#### 인증/인가
- ✅ 회원가입 (`POST /api/employees/signup`)
- ✅ 로그인 (`POST /api/employees/login`) - JWT 토큰 발급
- ✅ 내 정보 조회 (`GET /api/employees/me`)
- ✅ 직원 전용 대시보드 (`GET /api/employees/dashboard`)

---

## 🔐 보안 기능

### JWT 인증 시스템
- ✅ JWT 토큰 생성 (`JwtTokenProvider`)
- ✅ JWT 토큰 검증 (`JwtAuthenticationFilter`)
- ✅ SecurityContext에 인증 정보 저장
- ✅ 토큰 만료 시간: 1시간 (3600000ms)

### 역할 기반 접근 제어 (RBAC)
- ✅ `ROLE_EMPLOYER` - 사장님 권한
- ✅ `ROLE_EMPLOYEE` - 직원 권한
- ✅ `ROLE_ADMIN` - 관리자 권한 (준비됨)
- ✅ `@PreAuthorize` 어노테이션으로 엔드포인트 보호

### 비밀번호 보안
- ✅ BCrypt 암호화
- ✅ 평문 비밀번호는 저장하지 않음

---

## 📦 기술 스택 상세

### Backend Framework
- **Spring Boot**: 4.0.0
- **Java**: 21
- **Spring Security**: 7.0.0
- **Spring Data JPA**: 4.0.0

### 데이터베이스
- **MySQL**: 8.0.44
- **Hibernate**: 7.1.8.Final
- **JPA**: Jakarta Persistence 3.2.0

### 인증/보안
- **JWT**: jjwt 0.13.0
- **BCrypt**: Spring Security 내장

### 유틸리티
- **Lombok**: 1.18.42
- **MapStruct**: 1.6.3 (Entity ↔ DTO 변환)
- **Validation**: Jakarta Validation 3.1.1

---

## 🏛️ 아키텍처 패턴

### 레이어드 아키텍처
```
Controller (표현 계층)
    ↓
Service (비즈니스 계층)
    ↓
Repository (데이터 접근 계층)
    ↓
Database
```

### 역할 분담
- **Controller**: HTTP 요청/응답 처리, 인증 정보 추출
- **Service**: 비즈니스 로직, Entity ↔ DTO 변환 (MapStruct)
- **Repository**: 데이터베이스 접근
- **Mapper**: Entity와 DTO 간 변환 (MapStruct)

---

## 📝 주요 엔티티

### Employer (사장님)
```java
- id: Long (PK)
- email: String (unique, 로그인용)
- password: String (암호화)
- name: String
- phone: String
- storeName: String
- role: Role (ROLE_EMPLOYER)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Employee (직원)
```java
- id: Long (PK)
- email: String (unique, 로그인용)
- password: String (암호화)
- name: String
- phone: String
- employer: Employer (ManyToOne)
- role: Role (ROLE_EMPLOYEE)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

---

## 🔄 API 엔드포인트 목록

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

## 🛠️ 주요 설정

### application.properties
```properties
# 데이터베이스
spring.datasource.url=jdbc:mysql://localhost:3306/parttime_hr
spring.datasource.username=root
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=secret-key-for-hs256-part-timeHR-project
jwt.expiration=3600000
```

### Security 설정
- CSRF 비활성화 (JWT 사용)
- Stateless 세션 (JWT 기반)
- `/api/employers/signup`, `/api/employers/login` - 인증 불필요
- `/api/employees/signup`, `/api/employees/login` - 인증 불필요
- 나머지 엔드포인트 - 인증 필요

---

## ✨ 주요 특징

### 1. MapStruct 활용
- Entity ↔ DTO 자동 변환
- 컴파일 타임 코드 생성 (런타임 오버헤드 없음)
- 타입 안전성 보장

### 2. 전역 예외 처리
- `GlobalExceptionHandler`로 일관된 에러 응답
- Validation 에러 처리
- 비즈니스 로직 에러 처리

### 3. 역할 기반 접근 제어
- `@PreAuthorize` 어노테이션
- SecurityConfig에서 URL 기반 접근 제어
- JWT 토큰에 역할 정보 포함

### 4. 깔끔한 아키텍처
- Controller는 Service만 호출
- Service에서 Entity ↔ DTO 변환
- Repository는 데이터 접근만 담당

---

## 📊 데이터베이스 스키마

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

## 🧪 테스트 상태

### 완료된 테스트
- ✅ Employer 회원가입/로그인
- ✅ JWT 토큰 발급 및 검증
- ✅ 인증된 엔드포인트 접근
- ✅ 역할 기반 접근 제어
- ✅ Employee 회원가입/로그인
- ✅ 사장님이 직원 등록
- ✅ 사장님이 직원 목록 조회

### 테스트 도구
- Postman
- JWT.io (토큰 검증)

---

## 📚 문서

- `POSTMAN_TEST_GUIDE.md` - Postman 테스트 가이드
- `TEST_SCENARIOS.md` - 테스트 시나리오
- `PROJECT_SUMMARY.md` - 프로젝트 요약 (본 문서)

---

## 🚀 다음 단계 제안

### 추가 기능
1. 스케줄 관리
   - 근무 스케줄 등록/조회/수정
   - 직원별 스케줄 조회

2. 근무 시간 기록
   - 출근/퇴근 기록
   - 근무 시간 계산

3. 급여 관리
   - 시급 설정
   - 급여 계산
   - 급여 내역 조회

4. 알림 기능
   - 스케줄 변경 알림
   - 급여 지급 알림

5. 통계/리포트
   - 월별 근무 시간 통계
   - 급여 리포트

### 개선 사항
1. Refresh Token 구현
2. 비밀번호 변경 기능
3. 로그아웃 기능 (토큰 블랙리스트)
4. 이메일 인증
5. 프로필 수정 기능

---

## 📝 개발 이슈 해결 내역

1. ✅ Java 버전 불일치 (21 vs 1.8) → 21로 통일
2. ✅ JWT 필터 미등록 → SecurityConfig에 추가
3. ✅ 중복 컨트롤러 → EmployerSignupController 삭제
4. ✅ Employee 엔티티 비어있음 → 기본 구조 구현
5. ✅ MapStruct 미사용 → MapStruct 적용
6. ✅ Controller에서 변환 처리 → Service로 이동

---

## 🎓 학습 포인트

1. **Spring Security + JWT**: Stateless 인증 구현
2. **MapStruct**: Entity ↔ DTO 자동 변환
3. **레이어드 아키텍처**: 역할 분담 명확화
4. **역할 기반 접근 제어**: RBAC 구현
5. **전역 예외 처리**: 일관된 에러 응답

---

**작성일**: 2025-12-24  
**프로젝트 상태**: 기본 기능 구현 완료 ✅

