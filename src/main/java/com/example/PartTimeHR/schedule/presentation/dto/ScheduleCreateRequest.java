package com.example.PartTimeHR.schedule.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

// 근무 날짜는 startTime의 날짜에서 유도되므로 따로 받지 않는다
@Getter
public class ScheduleCreateRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
