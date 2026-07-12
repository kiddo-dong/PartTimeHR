package com.example.PartTimeHR.schedule.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScheduleResponse {

    private Long scheduleId;
    private Long employeeId;
    private String employeeName;

    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
