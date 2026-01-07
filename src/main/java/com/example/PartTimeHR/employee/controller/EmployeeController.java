package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.UpdateEmployeeRequest;
import com.example.PartTimeHR.employee.service.EmployeeService;
import com.example.PartTimeHR.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    // 현재 로그인한 직원 정보 조회 (모든 권한 접근 가능)
    @GetMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        EmployeeInfoResponse response = employeeService.getMyInfo(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

    // 직원만 접근 가능한 엔드포인트 (인가 필요)
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeInfoResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        EmployeeInfoResponse response = employeeService.getMyInfo(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

    // 직원 정보 수정 (모든 권한 접근 가능)
    @PutMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> updateEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {

        EmployeeInfoResponse response = employeeService.updateEmployee(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }

}

