package com.example.PartTimeHR.auth.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class EmailVerification {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "token")
    private String token;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Employer employer;


    public static EmailVerification create(Employer employer) {
        EmailVerification ev = new EmailVerification();
        ev.employer = employer;
        ev.email = employer.getEmail();  // ⚡ 여기 추가
        ev.token = UUID.randomUUID().toString();
        ev.expiredAt = LocalDateTime.now().plusMinutes(30);
        return ev;
    }


    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}