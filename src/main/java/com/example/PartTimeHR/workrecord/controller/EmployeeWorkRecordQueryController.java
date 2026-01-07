package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import com.example.PartTimeHR.workrecord.service.EmployeeWorkRecordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 직원 조회 전용(본인 - login 시 발행한 jwt 필요)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/work-records")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeWorkRecordQueryController {

    private final EmployeeWorkRecordQueryService employeeWorkRecordQueryService;

    // 직원 JWT(Role 및 Authentication)기반 GET 권한 처리하기
    @GetMapping("/today")
    public ResponseEntity<WorkRecordResponse> today(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        WorkRecordResponse response = employeeWorkRecordQueryService.today(userDetails.getEmail());

        return ResponseEntity.ok(response);
    }
}