package com.example.PartTimeHR.workrecord.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkRecordResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate workDate;
    private LocalDateTime clockInTime;
    private LocalDateTime breakStartTime;
    private LocalDateTime breakEndTime;
    private LocalDateTime clockOutTime;
    private String status;
    private Long totalWorkMinutes;      // 총 근무 시간
    private Long breakMinutes;          // 휴게 시간
    private Long actualWorkMinutes;     // 실 근무 시간
    private String memo;
    private int appliedHourlyWage;   // 시급
    private String appliedJobTitle;   // 직급
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

