package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.service.EmployeeMeService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeMeController {

    private final EmployeeMeService employeeMeService;

    // 직원 정보 조회
    @GetMapping
    public ResponseEntity<EmployeeInfoResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        EmployeeInfoResponse response = employeeMeService.getEmployeeInfo(userDetails.getId());

        return ResponseEntity.ok(response);
    }
}
