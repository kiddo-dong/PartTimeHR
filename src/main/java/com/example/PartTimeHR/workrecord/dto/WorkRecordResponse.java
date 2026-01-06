package com.example.PartTimeHR.workrecord.dto;

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
    private Double totalWorkHours;      // 총 근무 시간
    private Double breakHours;          // 휴게 시간
    private Double actualWorkHours;     // 실제 근무 시간 (총 - 휴게)
    private String memo;
    private int appliedHourlyWage;   // 시급
    private String appliedJobName;   // 직급
    private Double todayPay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

