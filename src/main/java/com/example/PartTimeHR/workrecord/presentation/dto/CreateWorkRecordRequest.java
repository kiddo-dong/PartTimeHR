package com.example.PartTimeHR.workrecord.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateWorkRecordRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate workDate;

    @NotNull
    private LocalDateTime clockInTime;

    private LocalDateTime breakStartTime;  // 선택

    private LocalDateTime breakEndTime;     // 선택

    private LocalDateTime clockOutTime;    // 선택

    private String memo;                   // 선택
}

