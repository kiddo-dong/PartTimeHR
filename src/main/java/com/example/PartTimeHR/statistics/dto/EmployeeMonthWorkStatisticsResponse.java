package com.example.PartTimeHR.statistics.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeMonthWorkStatisticsResponse {
    private LocalDate monthStartDate;   // 월 시작일
    private LocalDate monthEndDate;     // 월 종료일
    private Long totalWorkMinutes;      // 전체 근무시간(분)
    private Long totalPay;              // 전체 급여 합계
    private List<EmployeeWorkStatisticsResponse> employeeWorkStatistics;
}
