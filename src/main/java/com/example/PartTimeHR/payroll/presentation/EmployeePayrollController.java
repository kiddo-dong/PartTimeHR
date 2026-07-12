package com.example.PartTimeHR.payroll.presentation;

import com.example.PartTimeHR.payroll.application.PayrollService;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollDetailResponse;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 본인 급여 조회 (직원)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/payroll")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeePayrollController {

    private final PayrollService payrollService;

    @GetMapping
    public ResponseEntity<EmployeePayrollDetailResponse> getMyPayroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(
                payrollService.getMyPayroll(userDetails.getId(), from, to)
        );
    }
}
