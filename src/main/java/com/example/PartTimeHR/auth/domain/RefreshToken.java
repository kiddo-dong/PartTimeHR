package com.example.PartTimeHR.auth.domain;

import com.example.PartTimeHR.employer.domain.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 리프레시 토큰 (DB 저장 → 로그아웃 시 폐기 가능)
@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    // 유효 기간 14일
    private static final long VALIDITY_DAYS = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    // 사용자 식별 (Employer/Employee 테이블이 분리돼 있어 이메일 + 역할로 저장)
    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public static RefreshToken create(String email, Role role) {
        RefreshToken t = new RefreshToken();
        t.token = UUID.randomUUID().toString();
        t.email = email;
        t.role = role;
        t.expiredAt = LocalDateTime.now().plusDays(VALIDITY_DAYS);
        return t;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
