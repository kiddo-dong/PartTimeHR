package com.example.PartTimeHR.employer.controller;

import com.example.PartTimeHR.employer.dto.*;
import com.example.PartTimeHR.employer.service.EmployerService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerController {

    private final EmployerService employerService;

    //================= Employer CRUD Controller==================
    // 본인 정보 조회(사장님)
    @GetMapping("/me")
    public ResponseEntity<EmployerInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // jwt 인증 정보로 find
        EmployerInfoResponse response = employerService.getMyInfo(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }

    // 사장님 본인 정보 수정
    @PutMapping("/me")
    public ResponseEntity<EmployerInfoResponse> updateEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateEmployerRequest request
    ) {
        EmployerInfoResponse response = employerService.updateEmployer(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    // 사용자 정보 삭제 == 추후 확장 (연관 DB정보들 제거 후 사용자 정보 삭제)
    @DeleteMapping("/me")
    public void deleteEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

    }

    // ================ Employee CRUD 용 Controller ==================

    // 사장님이 직원 등록
    @PostMapping("/employees")
    public ResponseEntity<Void> registerEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterEmployeeRequest request
    ) {

        employerService.registerEmployee(userDetails.getEmail(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 단일 직원 정보 조회 - 단일
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long employeeId
    ) {
        EmployeeResponse response = employerService.getMyEmployee(userDetails.getId(), employeeId);

        return ResponseEntity.ok(response);
    }

    // 사장님의 직원 정보 리스트 조회 - 전체
    @GetMapping("/employees/all")
    public ResponseEntity<List<EmployeeListResponse>> getMyEmployees(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<EmployeeListResponse> response = employerService.getMyEmployees(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    // 지정한 직급의 직원 정보 조회 - 조건 전체
    @GetMapping("employees/jobtitle")
    public ResponseEntity<List<EmployeeListResponse>> getMyEmployeesJobTitle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String jobTitle
    ) {

        List<EmployeeListResponse> responses = employerService.getMyEmployeesJobTitle(userDetails.getId(), jobTitle);

        return ResponseEntity.ok(responses);
    }
}