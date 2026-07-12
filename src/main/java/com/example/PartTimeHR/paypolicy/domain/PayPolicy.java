package com.example.PartTimeHR.paypolicy.domain;

import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pay_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PayPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // findStore 소유
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private int hourlyWage;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime createdAt;

    // active를 여기서 덮어쓰지 않는다 (비활성 정책 생성이 불가능해짐)
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // 부분 수정: null인 필드는 기존 값 유지
    public void update(String jobTitle, Integer hourlyWage) {
        if (jobTitle != null) {
            this.jobTitle = jobTitle;
        }
        if (hourlyWage != null) {
            this.hourlyWage = hourlyWage;
        }
    }

}

