package com.example.PartTimeHR.payroll.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// 근무 기록 1건의 급여 내역
@Getter
@Builder
public class PayrollRecordResponse {
    private Long workRecordId;
    private LocalDate workDate;
    private int netWorkedMinutes;
    private int appliedHourlyWage;
    private String appliedJobTitle;
    private long pay;
}
