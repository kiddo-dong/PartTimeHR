package com.example.PartTimeHR.employer.controller;

import com.example.PartTimeHR.employer.dto.*;
import com.example.PartTimeHR.employer.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<EmployerInfoResponse> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // jwt 인증 정보로 find
        EmployerInfoResponse response = employerService.getMyInfo(email);

        return ResponseEntity.ok(response);
    }

    // 사장님만 접근 가능한 엔드포인트 (인가 필요)
    // 실제 통계는 GET /api/employers/statistics/dashboard 사용
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse response = DashboardResponse.builder()
                .message("사장님 대시보드에 오신 것을 환영합니다!")
                .description("실제 통계는 GET /api/employers/statistics/dashboard를 사용하세요.")
                .build();
        return ResponseEntity.ok(response);
    }

    // 모든 인증된 사용자 접근 가능
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ProfileResponse response = ProfileResponse.builder()
                .message("프로필 페이지입니다.")
                .email(authentication.getName())
                .authorities(authentication.getAuthorities().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    // 사장님이 직원 등록
    @PostMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> registerEmployee(
            @Valid @RequestBody RegisterEmployeeRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employerEmail = authentication.getName();

        employerService.registerEmployee(employerEmail, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 사장님의 직원 목록 조회
    @GetMapping("/employees")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<EmployeeListResponse>> getMyEmployees() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employerEmail = authentication.getName();

        List<EmployeeListResponse> response = employerService.getMyEmployees(employerEmail);

        return ResponseEntity.ok(response);
    }

    // 사장님 정보 수정
    @PutMapping("/me")
    public ResponseEntity<EmployerInfoResponse> updateEmployer(
            @Valid @RequestBody UpdateEmployerRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        EmployerInfoResponse response = employerService.updateEmployer(email, request);
        return ResponseEntity.ok(response);
    }
}

