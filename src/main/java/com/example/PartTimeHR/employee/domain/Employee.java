package com.example.PartTimeHR.employee.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 고용자(직)
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 로그인용 아이디(중복 불가)
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    // 비밀번호
    @Column(name = "password", nullable = false)
    private String password;

    // 직원 이름
    @Column(name = "employee_name", nullable = false, length = 30)
    private String name;

    // 전화번호
    @Column(name = "employee_phone", nullable = false, length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // 생성 시간
    @Column(name = "created_at")
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
}
