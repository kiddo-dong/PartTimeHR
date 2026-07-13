package com.example.PartTimeHR.invite.domain;

import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 매장 초대 코드. 매장당 하나씩 존재하고, 사장이 명시적으로 재발급하기 전까지
 * 만료 없이 유지된다 (벽보처럼 계속 붙여두고 여러 지원자가 같은 코드로 가입).
 * 유출 시에는 재발급으로 기존 코드를 무효화할 수 있다.
 */
@Entity
@Table(name = "store_invite_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Column(nullable = false, unique = true, length = 8)
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static StoreInviteCode create(Store store, String code) {
        StoreInviteCode invite = new StoreInviteCode();
        invite.store = store;
        invite.code = code;
        return invite;
    }
}
