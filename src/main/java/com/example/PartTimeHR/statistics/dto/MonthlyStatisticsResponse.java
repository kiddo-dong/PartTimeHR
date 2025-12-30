package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatisticsResponse {
    private Integer year;
    private Integer month;
    private Long totalWorkDays;              // 총 근무 일수
    private Double totalWorkHours;           // 총 근무 시간
    private Double totalActualWorkHours;     // 총 실제 근무 시간
    private Long totalEmployeeCount;         // 근무한 직원 수
    private List<EmployeeMonthlySummary> employeeSummaries; // 직원별 요약
}

