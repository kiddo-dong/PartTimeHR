package com.example.PartTimeHR.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class ScheduleResponse {

    private Long scheduleId;
    private Long employeeId;
    private String employeeName;

    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean confirmed;
}
