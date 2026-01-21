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

    // 스냅샷: 근무 당시 정책 정보
    @Column(nullable = false)
    private int appliedHourlyWage;   // 그날 기준 시급

    @Column(nullable = false, length = 50)
    private String appliedJobTitle;   // 그날 기준 직급

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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    /** 현재 진행 중인 근무인가 */
    public boolean isActive() {
        return clockOutTime == null;
    }

    /** 휴게 시작 가능 여부 */
    public boolean canStartBreak() {
        return status == WorkStatus.IN_PROGRESS
                && breakStartTime == null;
        // breakEndTime은 당연히 null
    }

    /** 휴게 종료 가능 여부 */
    public boolean canEndBreak() {
        return status == WorkStatus.ON_BREAK
                && breakStartTime != null
                && breakEndTime == null;
    }

    /** 퇴근 가능 여부 */
    public boolean canClockOut() {
        // 휴게가 없으면 바로 퇴근 가능
        if (breakStartTime == null) {
            return status == WorkStatus.IN_PROGRESS;
        }

        // 휴게가 있다면 반드시 종료 후 퇴근
        return status == WorkStatus.IN_PROGRESS
                && breakEndTime != null;
    }

    /* ======================
       상태 변경 메서드
       ====================== */

    /** 휴게 시작 */
    public void startBreak() {
        if (!canStartBreak()) {
            throw new IllegalStateException("휴게를 시작할 수 없는 상태입니다.");
        }
        this.breakStartTime = LocalDateTime.now();
        this.status = WorkStatus.ON_BREAK;
    }

    /** 휴게 종료 */
    public void endBreak() {
        if (!canEndBreak()) {
            throw new IllegalStateException("휴게를 종료할 수 없는 상태입니다.");
        }
        this.breakEndTime = LocalDateTime.now();
        this.status = WorkStatus.IN_PROGRESS;
    }

    /** 퇴근 */
    public void clockOut(LocalDateTime now) {
        if (!canClockOut()) {
            throw new IllegalStateException("퇴근할 수 없는 상태입니다.");
        }
        this.clockOutTime = now;
        this.status = WorkStatus.COMPLETED;
    }
}


