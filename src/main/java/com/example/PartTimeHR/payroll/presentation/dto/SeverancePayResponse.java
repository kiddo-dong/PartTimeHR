package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 퇴직금 추정 (퇴직급여보장법).
 * 법정 산식: 1일 평균임금 × 30일 × (재직일수 / 365)
 * - 평균임금: 기준일 이전 3개월 임금총액 ÷ 그 기간 총 일수
 *   (평균임금이 통상임금 추정치보다 낮으면 통상임금 사용)
 * - 추정치이며 실제 정산 시 노무사 확인 권장
 */
@Getter
@Builder
public class SeverancePayResponse {

    private Long employeeId;
    private String employeeName;

    private boolean eligible;         // 지급 대상 여부 (1년 이상 + 주 15시간 이상)
    private String ineligibleReason;  // 대상이 아닌 이유

    private LocalDate hiredAt;
    private LocalDate asOf;           // 산정 기준일 (퇴직 예정일)
    private long serviceDays;         // 재직일수

    private long averageDailyWage;    // 1일 평균임금 (통상임금 보정 반영)
    private long estimatedAmount;     // 퇴직금 추정액
}
