package com.example.PartTimeHR.statistics.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class EmployeeWeekWorkStatisticsResponse {
    private LocalDate weekStartDate;   // 주 시작일
    private LocalDate weekEndDate;     // 주 종료일
    private Long totalWorkMinutes;     // 전체 근무시간(분)
    private Long totalPay;             // 급여 전체 합계
    private List<EmployeeWorkStatisticsResponse> employeeWorkStatistics;
}