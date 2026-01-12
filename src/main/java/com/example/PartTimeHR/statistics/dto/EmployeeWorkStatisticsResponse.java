package com.example.PartTimeHR.statistics.dto;

import lombok.*;

@Getter
@RequiredArgsConstructor
@Builder
public class EmployeeWorkStatisticsResponse {
    private final String name;           // 직원 이름
    private final Long workDays;         // 근무일수 (서비스에서 count로 계산)
    private final Long totalWorkMinutes; // 총 근무 시간 (분)
    private final String jobTitle;       // 직급
    private final Integer hourlyWage;    // 시급
    private final Long totalPay;         // 사장님이 주휴 수당 사용 시(주휴 수당을 계산 하여 총 급여 설정)
}