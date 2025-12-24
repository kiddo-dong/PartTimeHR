package com.example.PartTimeHR.employer.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 고용주(사장님)
@Entity
@Table(
        name = "employer",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employer_email",
                        columnNames = "email"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employer {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인용 이메일 (중복 불가)
    @Column(nullable = false, length = 100)
    private String email;

    // 비밀번호 (암호화된 값만 저장)
    @Column(nullable = false)
    private String password;

    // 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    // 사장 이름
    @Column(name = "employer_name", nullable = false, length = 30)
    private String name;

    // 전화번호
    @Column(name = "employer_phone", nullable = false, length = 20)
    private String phone;

    // 가게 이름
    @Column(name = "store_name", nullable = false, length = 50)
    private String storeName;

    // 직원들
    // @OneToMany(mappedBy = "employer", fetch = FetchType.LAZY)
    // private List<Employee> employees = new ArrayList<>();

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
}
