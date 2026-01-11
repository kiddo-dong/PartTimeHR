package com.example.PartTimeHR.statistics.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class EmployeeMonthWorkStatisticsResponse {
    private final LocalDate monthStartDate;   // 월 시작일
    private final LocalDate monthEndDate;     // 월 종료일
    private final Long totalWorkMinutes;      // 전체 근무시간(분)
    private final Long totalPay;              // 전체 급여 합계
    private final List<EmployeeWorkStatisticsResponse> employeeWorkStatistics;
}
