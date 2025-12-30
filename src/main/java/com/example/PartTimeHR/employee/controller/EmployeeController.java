package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.EmployeeLoginRequest;
import com.example.PartTimeHR.employee.dto.UpdateEmployeeRequest;
import com.example.PartTimeHR.employee.service.EmployeeService;
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

    // 직원 회원가입은 제거됨 - 사장님이 직원을 등록하는 방식만 사용

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody EmployeeLoginRequest request
    ) {
        String token = employeeService.login(request);
        return ResponseEntity.ok(token);
    }

    // 현재 로그인한 직원 정보 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> getMyInfo(
            @AuthenticationPrincipal String email
    ) {

        EmployeeInfoResponse response = employeeService.getMyInfo(email);

        return ResponseEntity.ok(response);
    }

    // 직원만 접근 가능한 엔드포인트 (인가 필요)
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeInfoResponse> getDashboard(
            @AuthenticationPrincipal String email
    ) {

        EmployeeInfoResponse response = employeeService.getMyInfo(email);

        return ResponseEntity.ok(response);
    }

    // 직원 정보 수정
    @PutMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> updateEmployee(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {

        EmployeeInfoResponse response = employeeService.updateEmployee(email, request);
        return ResponseEntity.ok(response);
    }

}

