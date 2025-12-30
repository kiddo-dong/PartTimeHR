package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayEmployeeInfo {
    private Long employeeId;
    private String employeeName;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String status;  // IN_PROGRESS, ON_BREAK, COMPLETED
    private Double actualWorkHours;  // 현재까지 실제 근무 시간
}

