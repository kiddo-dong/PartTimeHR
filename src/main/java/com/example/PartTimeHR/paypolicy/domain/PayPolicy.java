package com.example.PartTimeHR.paypolicy.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PayPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 사장님
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // 직급명 (기본: "알바생")
    @Column(nullable = false)
    private String jobTitle;

    // 시급 (기본: 최저시급)
    @Column(nullable = false)
    private int hourlyWage;

    // 기본 정책 여부 (직원 생성 시 자동 연결)
    @Column(nullable = false)
    private boolean isDefault;

    // 사용 여부
    @Column(nullable = false)
    private boolean active;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}

