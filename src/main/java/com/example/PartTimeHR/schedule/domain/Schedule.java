package com.example.PartTimeHR.schedule.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

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

    // 어떤 매장의 스케줄인가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 누구의 스케줄인가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // 스케줄 적용 시작일
    @Column(nullable = false)
    private LocalDate startDate;

    // 스케줄 적용 종료일
    @Column(nullable = false)
    private LocalDate endDate;

    // 근무 요일
    @ElementCollection
    @CollectionTable(
            name = "schedule_working_day",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> workingDays = EnumSet.noneOf(DayOfWeek.class);
}
