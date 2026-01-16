package com.example.PartTimeHR.extrawork.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "extra_work",
        indexes = {
                @Index(name = "idx_extra_work_employee", columnList = "employee_id"),
                @Index(name = "idx_extra_work_store", columnList = "store_id"),
                @Index(name = "idx_extra_work_date", columnList = "work_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExtraWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 직원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // 추가 근무 날짜 (하루)
    @Column(nullable = false)
    private LocalDate workDate;

    // 사유 (선택)
    private String reason;

    @Column(nullable = false)
    private boolean approved;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.approved = true;
    }
}
