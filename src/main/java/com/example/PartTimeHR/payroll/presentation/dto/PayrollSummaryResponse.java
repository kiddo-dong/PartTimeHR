package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// 매장 전체 급여 요약
@Getter
@Builder
public class PayrollSummaryResponse {
    private Long storeId;
    private LocalDate from;
    private LocalDate to;

    // true면 "주휴 포함 시급" 계약 매장 - 주휴수당이 시급에 반영되어 별도 표기되지 않음
    private boolean weeklyAllowanceIncluded;

    private long totalPay;    // 매장 전체 지급 예정액

    private List<EmployeePayrollResponse> employees;
}
