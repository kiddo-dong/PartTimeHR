package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatisticsResponse {
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalWorkDays;              // 총 근무 일수
    private Double totalWorkHours;           // 총 근무 시간
    private Double totalActualWorkHours;     // 총 실제 근무 시간
    private Double averageWorkHoursPerDay;   // 일평균 근무 시간
}

