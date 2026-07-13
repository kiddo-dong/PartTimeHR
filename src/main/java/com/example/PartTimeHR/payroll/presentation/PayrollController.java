package com.example.PartTimeHR.payroll.presentation;

import com.example.PartTimeHR.payroll.application.PayrollService;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollDetailResponse;
import com.example.PartTimeHR.payroll.presentation.dto.PayrollSummaryResponse;
import com.example.PartTimeHR.payroll.presentation.dto.SeverancePayResponse;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 급여 계산 (사장)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/payroll")
@PreAuthorize("hasRole('EMPLOYER')")
public class PayrollController {

    private final PayrollService payrollService;

    // 매장 전체 직원 급여 요약
    @GetMapping
    public ResponseEntity<PayrollSummaryResponse> getStorePayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(
                payrollService.getStorePayroll(userDetails.getId(), storeId, from, to)
        );
    }

    // 직원별 급여 상세 (기록별 내역 포함)
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeePayrollDetailResponse> getEmployeePayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(
                payrollService.getEmployeePayroll(userDetails.getId(), storeId, employeeId, from, to)
        );
    }

    // 퇴직금 추정 (기준일 생략 시 오늘)
    @GetMapping("/employees/{employeeId}/severance")
    public ResponseEntity<SeverancePayResponse> getSeverancePay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf
    ) {
        return ResponseEntity.ok(
                payrollService.getSeverancePay(userDetails.getId(), storeId, employeeId, asOf)
        );
    }
}
