package com.example.PartTimeHR.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<DayOfWeek> workingDays;

}