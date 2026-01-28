package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.CreateEmployeeRequest;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.UpdateEmployeeRequest;
import com.example.PartTimeHR.employee.service.EmployeeService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Array;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/employees")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployeeController {

    private final EmployeeService employeeService;

    // ===== 생성 API =====
    // 직원 생성
    @PostMapping
    public ResponseEntity<EmployeeInfoResponse> createEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {

        EmployeeInfoResponse response = employeeService.createEmployee(storeId, request, userDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== 수정 API =====
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeInfoResponse> updateEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        EmployeeInfoResponse response = employeeService.updateEmployee(userDetails.getId(), storeId, employeeId, request);

        return ResponseEntity.ok(response);
    }

    // ===== 조회 API =====
    // 전체 직원 조회
    @GetMapping("/all")
    public ResponseEntity<List<EmployeeInfoResponse>> getAllEmployees(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ){
        List<EmployeeInfoResponse> responses = employeeService.getAllEmployees(userDetails.getId(), storeId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    // 직원 조회 - 단일
    @GetMapping
    public ResponseEntity<EmployeeInfoResponse> getEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @RequestParam Long employeeId
    ){
        EmployeeInfoResponse response = employeeService.getEmployee(userDetails.getId(), storeId, employeeId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}