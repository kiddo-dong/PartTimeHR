package com.example.PartTimeHR.employee.domain;

import com.example.PartTimeHR.auth.domain.Account;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 직원
// 인증 정보(email/password/role)는 Account가 갖고,
// Employee는 Account와 PK를 공유한다(@MapsId) — Employee.id == Account.id
@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @MapsId
    @JoinColumn(name = "id")
    private Account account;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    // 매장 소유
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_policy_id", nullable = false)
    private PayPolicy payPolicy;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    // 약정 주휴일 요일 (1=월 ~ 7=일, null이면 미지정)
    // 이 요일의 근무는 휴일근로 가산(5인 이상), 쉼은 결근이 아님
    @Column(name = "weekly_rest_day")
    private Integer weeklyRestDay;

    // 입사일 (퇴직금·연차 산정 기준)
    @Column(name = "hired_at")
    private LocalDate hiredAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* 비즈니스 */
    public void updateBasicInfo(String name, String phone) {
        if (name != null) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    public void changePayPolicy(PayPolicy policy) {
        this.payPolicy = policy;
    }

    public void assignWeeklyRestDay(Integer weeklyRestDay) {
        this.weeklyRestDay = weeklyRestDay;
    }
}
