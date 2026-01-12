package com.example.PartTimeHR.mail.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토큰 문자열
    @Column(nullable = false, unique = true)
    private String token;

    // 유저
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employer;

    // 만료 시간
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    // ----------------------------
    // 토큰 생성
    // ----------------------------
    public static PasswordResetToken create(Employer employer) {
        PasswordResetToken t = new PasswordResetToken();
        t.employer = employer;
        t.token = UUID.randomUUID().toString();
        t.expiredAt = LocalDateTime.now().plusMinutes(30); // 30분 유효
        return t;
    }

    // ----------------------------
    // 만료 체크
    // ----------------------------
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}

