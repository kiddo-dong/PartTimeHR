package com.example.PartTimeHR.store.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employer.domain.Employer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;                // 기존 name
    @Column(name = "phone")
    private String phone;  // 가게 전화번호
    @Column(name = "address")
    private String address;
    @Column(name = "week_start_day", nullable = false)
    private Integer weekStartDay; // 주 시작일
    @Column(name = "weekly_pay_applicable", nullable = false)
    private Boolean weeklyPayApplicable; // (주휴 수당 제공 여부

    // 사장
    @ManyToOne
    @JoinColumn(name="employer_id")
    private Employer employer;

    // 직원들
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    // 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간
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


    /* 비즈니스 */
    // 부분 수정: null인 필드는 기존 값 유지
    public void update(
            String name,
            String phone,
            String address,
            Integer weekStartDay,
            Boolean weeklyPayApplicable
    ) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (address != null) this.address = address;
        if (weekStartDay != null) this.weekStartDay = weekStartDay;
        if (weeklyPayApplicable != null) this.weeklyPayApplicable = weeklyPayApplicable;
    }
}
