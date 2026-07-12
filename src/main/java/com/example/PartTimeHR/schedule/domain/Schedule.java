package com.example.PartTimeHR.schedule.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 근무 예정 (스케줄).
 * workDate는 항상 startTime의 날짜에서 유도된다 — 따로 받지 않아
 * 날짜-시간 불일치가 원천적으로 불가능하다.
 */
@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 근무 직원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // 근무 날짜 (startTime에서 유도, 날짜 기준 조회용 인덱스 컬럼)
    @Column(nullable = false)
    private LocalDate workDate;

    // 근무 시작 / 종료
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // 생성 / 수정 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* 팩토리 - 생성 시 workDate를 startTime에서 유도 */
    public static Schedule create(
            Store store,
            Employee employee,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        Schedule schedule = new Schedule();
        schedule.store = store;
        schedule.employee = employee;
        schedule.startTime = startTime;
        schedule.endTime = endTime;
        schedule.workDate = startTime.toLocalDate();
        return schedule;
    }

    /* 비즈니스 로직 - 시간이 바뀌면 workDate도 따라간다 */
    public void updateTime(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.workDate = startTime.toLocalDate();
    }
}
