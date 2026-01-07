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

// 직원만 접근 가능한 엔드포인트 (JWT 필요)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        EmployeeInfoResponse response = employeeService.getMyInfo(userDetails.getId());

        return ResponseEntity.ok(response);
    }
}

