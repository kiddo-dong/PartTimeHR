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
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false)
    private String storeName;                // 기존 storeName
    @Column(name = "store_phone")
    private String storePhone;  // 가게 전화번호
    @Column(name = "store_address")
    private String storeAddress;
    @Column(name = "week_start_day", nullable = false)
    private Integer weekStartDay;
    @Column(name = "weekly_pay_applicable", nullable = false)
    private Boolean weeklyPayApplicable = false;// 기존 weeklyPayApplicable (주휴 수당 제공 여부)

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


    // 팩토리 메소드
    public static Store create(
            String storeName,
            String storePhone,
            String storeAddress,
            Integer weekStartDay,
            Boolean weeklyPayApplicable,
            Employer employer
    ) {
        Store store = new Store();
        store.storeName = storeName;
        store.storePhone = storePhone;
        store.storeAddress = storeAddress;
        store.weekStartDay = weekStartDay;
        store.weeklyPayApplicable = weeklyPayApplicable;
        store.employer = employer;
        return store;
    }
}
