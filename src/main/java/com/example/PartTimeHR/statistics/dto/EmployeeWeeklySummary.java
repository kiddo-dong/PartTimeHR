package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWeeklySummary {
    private Long employeeId;
    private String employeeName;
    private Long workDays;                   // 근무 일수
    private Double totalWorkHours;           // 총 근무 시간
    private Double totalActualWorkHours;     // 총 실제 근무 시간
    private Double averageWorkHoursPerDay;   // 일평균 근무 시간
}

