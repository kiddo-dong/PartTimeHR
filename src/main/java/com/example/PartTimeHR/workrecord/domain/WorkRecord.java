package com.example.PartTimeHR.workrecord.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 출근 기록
@Entity
@Table(name = "work_record")
@Getter
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

    // 휴게 누적 시간 (분) 없으면 0
    @Column(name = "total_break_minutes", nullable = false)
    private int totalBreakMinutes;

    // 총 근무 시간 (분) 퇴근 시 확정
    @Column(name = "total_worked_minutes", nullable = false)
    private int totalWorkedMinutes;

    // 실근무 시간 (분)
    @Column(name = "net_worked_minutes", nullable = false)
    private int netWorkedMinutes;

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

    /**
     * 휴게 시작 가능 여부.
     * 근무 중이면 언제든 가능 — 휴게 시간은 totalBreakMinutes에 누적되므로
     * 하루에 여러 번 쉴 수 있다. (breakStartTime/breakEndTime은 마지막 휴게의 시각)
     */
    public boolean canStartBreak() {
        return status == WorkStatus.IN_PROGRESS;
    }

    /** 휴게 종료 가능 여부 */
    public boolean canEndBreak() {
        return status == WorkStatus.ON_BREAK;
    }

    /** 퇴근 가능 여부 (휴게 중이면 종료 후 퇴근) */
    public boolean canClockOut() {
        return status == WorkStatus.IN_PROGRESS;
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

    /** 휴게 종료 — 이번 휴게 시간을 누적하고 근무 상태로 복귀 */
    public void endBreak() {
        if (!canEndBreak()) {
            throw new IllegalStateException("휴게를 종료할 수 없는 상태입니다.");
        }
        this.breakEndTime = LocalDateTime.now();
        this.totalBreakMinutes += (int) Duration.between(breakStartTime, breakEndTime).toMinutes();
        this.status = WorkStatus.IN_PROGRESS;
    }

    /** 퇴근 */
    public void clockOut(LocalDateTime now) {
        if (!canClockOut()) {
            throw new IllegalStateException("퇴근할 수 없는 상태입니다.");
        }
        this.clockOutTime = now;
        this.status = WorkStatus.COMPLETED;

        recalculateMinutes();
    }

    /**
     * 시간 필드 간의 순서 검증. 수동 생성/수정 시 호출.
     * (검증 없이는 퇴근 < 출근 같은 입력으로 음수 근무 시간이 저장될 수 있다)
     */
    public void validateTimes() {
        if (clockOutTime != null && !clockInTime.isBefore(clockOutTime)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간 이후여야 합니다.");
        }
        if (breakStartTime != null && breakStartTime.isBefore(clockInTime)) {
            throw new IllegalArgumentException("휴게는 출근 이후에 시작해야 합니다.");
        }
        if (breakStartTime != null && breakEndTime != null && breakEndTime.isBefore(breakStartTime)) {
            throw new IllegalArgumentException("휴게 종료는 휴게 시작 이후여야 합니다.");
        }
        if (clockOutTime != null && breakEndTime != null && breakEndTime.isAfter(clockOutTime)) {
            throw new IllegalArgumentException("휴게는 퇴근 이전에 끝나야 합니다.");
        }
    }

    /**
     * 시간 필드로부터 상태를 다시 도출한다. 수동 생성/수정 시 호출.
     * (수정으로 퇴근 시간을 넣었는데 status가 IN_PROGRESS로 남는 불일치 방지)
     */
    public void refreshStatus() {
        if (clockOutTime != null) {
            this.status = WorkStatus.COMPLETED;
        } else if (breakStartTime != null && breakEndTime == null) {
            this.status = WorkStatus.ON_BREAK;
        } else {
            this.status = WorkStatus.IN_PROGRESS;
        }
    }

    /**
     * 수동 입력된 휴게 시작/종료 쌍으로 누적 휴게 시간을 덮어쓴다.
     * 관리자가 근무 기록을 직접 생성/수정할 때만 사용.
     */
    public void applyBreakFromTimes() {
        if (breakStartTime != null && breakEndTime != null) {
            this.totalBreakMinutes = (int) Duration.between(breakStartTime, breakEndTime).toMinutes();
        }
    }

    /**
     * 근무 시간 집계를 확정한다. (totalBreakMinutes는 이미 누적된 값을 사용)
     * 퇴근 시점과, 고용주가 기록을 수동으로 수정한 시점에 호출한다.
     */
    public void recalculateMinutes() {
        Long total = getTotalWorkMinutes();
        this.totalWorkedMinutes = total == null ? 0 : total.intValue();

        Long net = getActualWorkMinutes();
        this.netWorkedMinutes = net == null ? 0 : net.intValue();
    }


    public Long getTotalWorkMinutes() {
        if (clockInTime == null || clockOutTime == null) {
            return null;
        }
        return Duration.between(clockInTime, clockOutTime).toMinutes();
    }

    /** 누적 휴게 시간 (분) */
    public Long getBreakMinutes() {
        return (long) totalBreakMinutes;
    }

    public Long getActualWorkMinutes() {
        Long total = getTotalWorkMinutes();
        if (total == null) return null;
        return Math.max(total - getBreakMinutes(), 0L);
    }
}


