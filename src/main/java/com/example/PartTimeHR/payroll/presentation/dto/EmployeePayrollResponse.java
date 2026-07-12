package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

// 직원 1명의 급여 요약 (매장 전체 조회용)
@Getter
@Builder
public class EmployeePayrollResponse {
    private Long employeeId;
    private String employeeName;
    private int recordCount;          // 급여에 포함된 근무 기록 수 (퇴근 완료분)
    private int totalNetMinutes;      // 실근무 합계 (분)
    private long basePay;             // 기본급
    private long weeklyAllowance;     // 주휴수당
    private long overtimeAllowance;   // 연장근로 가산 (5인 이상 사업장)
    private long nightAllowance;      // 야간근로 가산 (5인 이상 사업장)
    private long totalPay;            // 합계
}
