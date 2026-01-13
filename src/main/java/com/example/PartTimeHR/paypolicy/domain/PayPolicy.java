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

    // 🔥 Store 소유
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

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

}

