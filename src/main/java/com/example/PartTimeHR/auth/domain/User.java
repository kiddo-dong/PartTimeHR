package com.example.PartTimeHR.auth.domain;

import com.example.PartTimeHR.employer.domain.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증 신원 (이메일/비밀번호/역할/이메일 인증 상태/활성화 여부).
 * Employer/Employee는 이 User와 PK를 공유(@MapsId)하고 각자의 도메인
 * 데이터(매장 소유/소속, 시급정책 등)만 갖는다. 로그인·이메일 중복 검사가
 * 이 테이블 하나로 통일된다 (예전에는 Employer/Employee 테이블에 이메일이
 * 중복 저장돼 있어 두 테이블을 매번 함께 조회/검사해야 했다).
 */
@Entity
@Table(
        name = "user",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    // 비밀번호 (암호화된 값만 저장)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified;

    /**
     * 로그인 가능 여부. 매장 초대코드로 가입한 직원은 사장 승인 전까지 false다
     * (emailVerified와는 별개 게이트 - 이메일 인증 개념이 없는 직원에게도 적용된다).
     * 사장 직접 등록/사장 본인 가입은 즉시 true.
     */
    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static User create(String email, String encodedPassword, Role role, boolean emailVerified, boolean active) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.role = role;
        user.emailVerified = emailVerified;
        user.active = active;
        return user;
    }

    // 암호화된 비밀번호만 받는다
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void activate() {
        this.active = true;
    }
}
