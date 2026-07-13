package com.example.PartTimeHR.leave.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 연차 잔여 (근로기준법 제60조, 상시 5인 이상).
 * - 1년 미만: 개근한 달마다 1일 (최대 11일)
 * - 1년 이상: 연 15일 + 3년차부터 2년마다 1일 가산 (최대 25일), 현재 연차년도 기준
 * - 이월 없음(단순화), 5인 미만 사업장은 법정 연차 없음
 */
@Getter
@Builder
public class LeaveBalanceResponse {
    private Long employeeId;
    private String employeeName;
    private LocalDate hiredAt;

    private boolean applicable;       // 법정 연차 적용 대상 여부 (5인 이상)
    private int grantedDays;          // 발생 연차
    private int usedDays;             // 사용(승인)한 연차
    private int remainingDays;        // 잔여
}
