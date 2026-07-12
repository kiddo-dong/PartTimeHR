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

    private long totalPay;    // 매장 전체 지급 예정액

    private List<EmployeePayrollResponse> employees;
}
