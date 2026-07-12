package com.example.PartTimeHR.schedule.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class ScheduleCreateRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate workDate;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
