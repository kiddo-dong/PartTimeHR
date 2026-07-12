package com.example.PartTimeHR.workrecord.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 근무 세션 (출근~퇴근 1회).
 *
 * 설계 원칙: 저장하는 것은 사실(시각)뿐이다.
 * - 상태(status)는 clockOutTime과 열린 휴게 여부에서 유도 → 불일치가 불가능
 * - 휴게는 WorkBreak(1:N)로 이력 보존, 누적 분은 파생값
 * - 총/실 근무 분도 파생값 (급여·통계·응답이 같은 근원을 사용)
 */
@Entity
@Table(name = "work_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkRecord {

    private static final String AUTO_CLOSE_MEMO = "[미퇴근 자동 마감]";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    // null이면 근무 진행 중
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    // 스냅샷: 근무 당시 정책 정보 (정책이 나중에 바뀌어도 급여 이력 안전)
    @Column(nullable = false)
    private int appliedHourlyWage;

    @Column(nullable = false, length = 50)
    private String appliedJobTitle;

    // 메모 (고용주 수정, 자동 마감 표시 등)
    @Column(length = 500)
    private String memo;

    @OneToMany(mappedBy = "workRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkBreak> breaks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    /* ======================
       파생 상태
       ====================== */

    public WorkStatus getStatus() {
        if (clockOutTime != null) {
            return WorkStatus.COMPLETED;
        }
        if (findOpenBreak() != null) {
            return WorkStatus.ON_BREAK;
        }
        return WorkStatus.IN_PROGRESS;
    }

    /** 현재 진행 중인 근무인가 */
    public boolean isActive() {
        return clockOutTime == null;
    }

    private WorkBreak findOpenBreak() {
        return breaks.stream()
                .filter(WorkBreak::isOpen)
                .findFirst()
                .orElse(null);
    }

    /* ======================
       상태 변경
       ====================== */

    /** 휴게 시작 - 근무 중이면 몇 번이든 가능 */
    public void startBreak(LocalDateTime now) {
        if (getStatus() != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("휴게를 시작할 수 없는 상태입니다.");
        }
        breaks.add(WorkBreak.open(this, now));
    }

    /** 휴게 종료 */
    public void endBreak(LocalDateTime now) {
        WorkBreak open = findOpenBreak();
        if (clockOutTime != null || open == null) {
            throw new IllegalStateException("휴게를 종료할 수 없는 상태입니다.");
        }
        open.close(now);
    }

    /** 퇴근 (휴게 중이면 종료 후 퇴근) */
    public void clockOut(LocalDateTime now) {
        if (getStatus() != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("퇴근할 수 없는 상태입니다.");
        }
        this.clockOutTime = now;
    }

    /**
     * 미퇴근 자동 마감 - 퇴근을 찍지 않은 채 다음 출근을 시도할 때 호출.
     * 근무일 자정 직전(23:59)으로 마감하고 메모에 표시한다.
     * 열린 휴게는 마감 시각까지를 휴게로 본다.
     */
    public void autoClose() {
        LocalDateTime closeTime = workDate.atTime(23, 59);
        if (!clockInTime.isBefore(closeTime)) {
            closeTime = clockInTime;
        }

        WorkBreak open = findOpenBreak();
        if (open != null) {
            open.close(open.getStartTime().isBefore(closeTime) ? closeTime : open.getStartTime());
        }

        this.clockOutTime = closeTime;
        this.memo = (memo == null || memo.isBlank())
                ? AUTO_CLOSE_MEMO
                : memo + " " + AUTO_CLOSE_MEMO;
    }

    /* ======================
       수동 생성/수정 (관리자)
       ====================== */

    /** 부분 수정: null인 필드는 기존 값 유지 */
    public void updateManually(LocalDateTime clockInTime, LocalDateTime clockOutTime, String memo) {
        if (clockInTime != null) this.clockInTime = clockInTime;
        if (clockOutTime != null) this.clockOutTime = clockOutTime;
        if (memo != null) this.memo = memo;
    }

    /** 휴게 전체를 입력된 한 쌍으로 교체 (수동 입력용) */
    public void replaceBreaks(LocalDateTime breakStart, LocalDateTime breakEnd) {
        breaks.clear();
        if (breakStart != null) {
            breaks.add(breakEnd == null
                    ? WorkBreak.open(this, breakStart)
                    : WorkBreak.closed(this, breakStart, breakEnd));
        }
    }

    /** 시간 필드 간의 순서 검증. 수동 생성/수정 시 호출 */
    public void validateTimes() {
        if (clockOutTime != null && !clockInTime.isBefore(clockOutTime)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간 이후여야 합니다.");
        }
        for (WorkBreak b : breaks) {
            if (b.getStartTime().isBefore(clockInTime)) {
                throw new IllegalArgumentException("휴게는 출근 이후에 시작해야 합니다.");
            }
            if (b.getEndTime() != null && b.getEndTime().isBefore(b.getStartTime())) {
                throw new IllegalArgumentException("휴게 종료는 휴게 시작 이후여야 합니다.");
            }
            if (clockOutTime != null && b.getEndTime() != null && b.getEndTime().isAfter(clockOutTime)) {
                throw new IllegalArgumentException("휴게는 퇴근 이전에 끝나야 합니다.");
            }
        }
    }

    /* ======================
       파생 집계 (급여·통계·응답 공통 근원)
       ====================== */

    /** 총 구속 시간(분). 퇴근 전이면 null */
    public Long getTotalWorkMinutes() {
        if (clockOutTime == null) {
            return null;
        }
        return Duration.between(clockInTime, clockOutTime).toMinutes();
    }

    /** 누적 휴게 시간(분). 종료된 휴게만 합산 */
    public long getBreakMinutes() {
        return breaks.stream().mapToLong(WorkBreak::minutes).sum();
    }

    /** 실근무 시간(분). 퇴근 전이면 null */
    public Long getActualWorkMinutes() {
        Long total = getTotalWorkMinutes();
        if (total == null) {
            return null;
        }
        return Math.max(total - getBreakMinutes(), 0L);
    }

    /* 응답 호환용: 마지막 휴게의 시작/종료 시각 */

    public LocalDateTime getBreakStartTime() {
        return lastBreak() == null ? null : lastBreak().getStartTime();
    }

    public LocalDateTime getBreakEndTime() {
        return lastBreak() == null ? null : lastBreak().getEndTime();
    }

    private WorkBreak lastBreak() {
        return breaks.stream()
                .max(Comparator.comparing(WorkBreak::getStartTime))
                .orElse(null);
    }
}
