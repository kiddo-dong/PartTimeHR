package com.example.PartTimeHR.schedule.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Store store;

    @ManyToOne(fetch = LAZY)
    private Employee employee;

    private LocalDate workDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status; // PLANNED, CANCELED

    private LocalDateTime createdAt;
}
