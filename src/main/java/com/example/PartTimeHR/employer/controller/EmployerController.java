package com.example.PartTimeHR.employer.controller;

import com.example.PartTimeHR.employer.dto.*;
import com.example.PartTimeHR.employer.service.EmployerService;
import com.example.PartTimeHR.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers")
public class EmployerController {

    private final EmployerService employerService;

    // 현재 로그인 한 사장님 정보 조회
    // 현재 로그인한 사용자 정보 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<EmployerInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // jwt 인증 정보로 find
        EmployerInfoResponse response = employerService.getMyInfo(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

    // 사장님이 직원 등록
    @PostMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> registerEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterEmployeeRequest request
    ) {

        employerService.registerEmployee(userDetails.getEmail(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 사장님의 직원 목록 조회
    @GetMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<EmployeeListResponse>> getMyEmployees(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<EmployeeListResponse> response = employerService.getMyEmployees(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

    // 사장님 정보 수정
    @PutMapping("/me")
    public ResponseEntity<EmployerInfoResponse> updateEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateEmployerRequest request
    ) {
        EmployerInfoResponse response = employerService.updateEmployer(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }
}