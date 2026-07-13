package com.example.PartTimeHR.auth.domain;

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

    // User.id 참조 (Employer/Employee가 User와 PK를 공유하므로
    // 이 값이 곧 Employer.id / Employee.id와 같다)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public static RefreshToken create(Long userId) {
        RefreshToken t = new RefreshToken();
        t.token = UUID.randomUUID().toString();
        t.userId = userId;
        t.expiredAt = LocalDateTime.now().plusDays(VALIDITY_DAYS);
        return t;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
