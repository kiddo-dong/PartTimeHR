package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// 직원 1명의 급여 상세 (기록별 내역 포함)
@Getter
@Builder
public class EmployeePayrollDetailResponse {
    private Long employeeId;
    private String employeeName;

    private LocalDate from;
    private LocalDate to;

    // true면 "주휴 포함 시급" 계약 매장 - 주휴수당이 시급에 반영되어 별도 표기되지 않음
    private boolean weeklyAllowanceIncluded;

    private int recordCount;
    private int totalNetMinutes;
    private long basePay;
    private long weeklyAllowance;
    private long overtimeAllowance;   // 연장근로 가산 (5인 이상 사업장)
    private long nightAllowance;      // 야간근로 가산 (5인 이상 사업장)
    private long totalPay;

    private List<PayrollRecordResponse> records;
}
