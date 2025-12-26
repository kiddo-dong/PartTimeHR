package com.example.PartTimeHR.workrecord.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 출근 기록
@Entity
@Table(name = "work_record")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkRecord {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 직원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // 근무 날짜
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // 출근 시간
    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    // 휴게 시작 시간 (선택)
    @Column(name = "break_start_time")
    private LocalDateTime breakStartTime;

    // 휴게 끝 시간 (선택)
    @Column(name = "break_end_time")
    private LocalDateTime breakEndTime;

    // 퇴근 시간 (선택)
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    // 근무 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkStatus status;

    // 메모 (고용주가 수정 시 사용)
    @Column(length = 500)
    private String memo;

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

