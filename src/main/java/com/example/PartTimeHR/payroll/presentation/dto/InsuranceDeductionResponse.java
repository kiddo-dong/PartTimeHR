package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 4대보험 근로자 부담분 공제 추정.
 * ⚠ 추정치 한계:
 * - 가입 대상 판정(월 60시간 미만 국민연금·건강보험 제외 등)은 하지 않고 요율만 적용
 * - 소득세·지방소득세(간이세액표)는 미포함
 * - 요율은 매년 변경되므로 환경변수로 갱신 필요
 */
@Getter
@Builder
public class InsuranceDeductionResponse {
    private long nationalPension;      // 국민연금 (4.5%)
    private long healthInsurance;      // 건강보험 (3.545%)
    private long longTermCare;         // 장기요양 (건강보험료의 12.95%)
    private long employmentInsurance;  // 고용보험 (0.9%)
    private long totalDeduction;       // 공제 합계
    private long estimatedNetPay;      // 공제 후 실수령 추정액 (세금 미반영)
}
