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

    /**
     * 주휴수당이 시급에 포함된 계약인지 여부.
     * 주휴수당(근로기준법 제55조)은 요건 충족 시 지급 의무라 끌 수 있는 옵션이 아니다.
     * - false(기본): 법정 기본 - 주휴수당을 별도 계산해 지급
     * - true: "주휴 포함 시급" 계약 - 별도 계산하지 않음
     *   (이 경우 시급은 최저임금 × 1.2 이상이어야 함 - PayPolicyService에서 검증)
     */
    @Builder.Default
    @Column(name = "weekly_allowance_included", nullable = false)
    private Boolean weeklyAllowanceIncluded = false;

    // 상시 근로자 5인 이상 여부 - 연장/야간 가산수당(근로기준법 제56조)은
    // 5인 이상 사업장에만 적용된다 (주휴수당은 인원과 무관하게 적용)
    @Builder.Default
    @Column(name = "five_or_more_employees", nullable = false)
    private Boolean fiveOrMoreEmployees = false;

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
            Boolean weeklyAllowanceIncluded,
            Boolean fiveOrMoreEmployees
    ) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (address != null) this.address = address;
        if (weekStartDay != null) this.weekStartDay = weekStartDay;
        if (weeklyAllowanceIncluded != null) this.weeklyAllowanceIncluded = weeklyAllowanceIncluded;
        if (fiveOrMoreEmployees != null) this.fiveOrMoreEmployees = fiveOrMoreEmployees;
    }
}
