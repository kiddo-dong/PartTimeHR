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

    @Column(name = "name", nullable = false)
    private String name;                // 기존 name
    @Column(name = "phone")
    private String phone;  // 가게 전화번호
    @Column(name = "address")
    private String address;
    @Column(name = "week_start_day", nullable = false)
    private Integer weekStartDay; // 주 시작일
    @Column(name = "weekly_pay_applicable", nullable = false)
    private Boolean weeklyPayApplicable; // 주휴 수당 제공 여부

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
        store.name = storeName;
        store.phone = storePhone;
        store.address = storeAddress;
        store.weekStartDay = weekStartDay;
        store.weeklyPayApplicable = weeklyPayApplicable;
        store.employer = employer;
        return store;
    }
}
