package com.example.PartTimeHR.employer.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 고용주(사장님)
@Entity
@Table(name = "employer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 로그인용 아이디 (중복 불가)
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    // 비밀번호
    @Column(name = "password", nullable = false)
    private String password;

    // 사장 이름
    @Column(name = "employer_name", nullable = false, length = 30)
    private String name;

    // 전화번호
    @Column(name = "employer_phone", nullable = false, length = 20)
    private String phone;

    // 가게 이름
    @Column(name = "store_name", nullable = false, length = 50)
    private String storeName;

    @OneToMany(mappedBy = "employer", fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

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
