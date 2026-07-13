package com.example.PartTimeHR.employer.domain;

import com.example.PartTimeHR.auth.domain.User;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 고용주(사장님)
// 인증 정보(email/password/role/emailVerified)는 User가 갖고,
// Employer는 User와 PK를 공유한다(@MapsId) — Employer.id == User.id
@Entity
@Table(name = "employer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employer {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // 사장 이름
    @Column(name = "employer_name", nullable = false, length = 30)
    private String name;

    // 전화번호
    @Column(name = "employer_phone", nullable = false, length = 20)
    private String phone;

    @OneToMany(mappedBy = "employer", fetch = FetchType.LAZY)
    private List<Store> stores = new ArrayList<>();

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
    public void updateBasicInfo(String name, String phone) {
        if (name != null) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }
}
