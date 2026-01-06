package com.example.PartTimeHR.employee.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 직원
@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee {
    
    // 수정 가능한 필드에 대한 setter
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인용 이메일 (중복 불가)
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    // 비밀번호 (암호화된 값만 저장)
    @Column(nullable = false)
    private String password;

    // 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    // 직원 이름
    @Column(name = "employee_name", nullable = false, length = 30)
    private String name;

    // 전화번호
    @Column(name = "employee_phone", nullable = false, length = 20)
    private String phone;

    // 고용주 (사장님)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // 직원이 현재 적용받는 정책(직급 / 시급)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_policy_id", nullable = false)
    private PayPolicy payPolicy;

    // 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* =======================
       생성 / 수정 시점 처리
       ======================= */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setPayPolicy(PayPolicy payPolicy) {
        this.payPolicy = payPolicy;
    }
}
