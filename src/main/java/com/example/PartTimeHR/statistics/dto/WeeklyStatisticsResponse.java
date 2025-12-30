package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyStatisticsResponse {
    private LocalDate weekStartDate;         // 주간 시작일
    private LocalDate weekEndDate;           // 주간 종료일
    private Integer weekStartDay;            // 주간 시작 요일 (1=월요일, 7=일요일)
    private Long totalWorkDays;              // 총 근무 일수
    private Double totalWorkHours;           // 총 근무 시간
    private Double totalActualWorkHours;     // 총 실제 근무 시간
    private Long totalEmployeeCount;         // 근무한 직원 수
    private List<EmployeeWeeklySummary> employeeSummaries; // 직원별 요약
}

