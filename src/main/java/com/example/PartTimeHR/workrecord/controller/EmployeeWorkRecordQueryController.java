package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployeeWorkRecordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 직원 조회 전용(본인 - login 시 발행한 jwt 필요)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/work-records")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeWorkRecordQueryController {

    private final EmployeeWorkRecordQueryService employeeWorkRecordQueryService;

    // 직원 JWT(Role 및 Authentication)기반 GET 권한 처리하기

    // 오늘 출근 기록
    @GetMapping("/today")
    public ResponseEntity<List<WorkRecordResponse>> today(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        List<WorkRecordResponse> response = employeeWorkRecordQueryService.today(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    // 특정 기간 출근 기록
    @GetMapping("/period")
    public ResponseEntity<List<WorkRecordResponse>> period(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<WorkRecordResponse> responses =
                employeeWorkRecordQueryService
                        .findByPeriod(userDetails.getId(), startDate, endDate);

        return ResponseEntity.ok(responses);
    }

}