package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.EmployeeLoginRequest;
import com.example.PartTimeHR.employee.dto.EmployeeSignupRequest;
import com.example.PartTimeHR.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody EmployeeSignupRequest request
    ) {
        employeeService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody EmployeeLoginRequest request
    ) {
        String token = employeeService.login(request);
        return ResponseEntity.ok(token);
    }

    // 현재 로그인한 직원 정보 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<EmployeeInfoResponse> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        EmployeeInfoResponse response = employeeService.getMyInfo(email);

        return ResponseEntity.ok(response);
    }

    // 직원만 접근 가능한 엔드포인트 (인가 필요)
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeInfoResponse> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        EmployeeInfoResponse response = employeeService.getMyInfo(email);

        return ResponseEntity.ok(response);
    }
}

