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

    // 오늘 출근 기록
    @GetMapping("/today")
    public ResponseEntity<List<WorkRecordResponse>> today(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<WorkRecordResponse> response = employeeWorkRecordQueryService.today(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    // 주별 조회
    @GetMapping("/week")
    public ResponseEntity<List<WorkRecordResponse>> week(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int weekOffset
    ) {

        // weekOffset = 0: 이번 주, -1: 이전 주, 1: 다음 주 등
        List<WorkRecordResponse> response = employeeWorkRecordQueryService.findByWeek(userDetails.getId(), weekOffset);

        return ResponseEntity.ok(response);
    }


    // 월별 조회
    @GetMapping("/month")
    public ResponseEntity<List<WorkRecordResponse>> month(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int monthOffset
    ) {
        // monthOffset = 0: 이번 달, -1: 지난 달, 1: 다음 달 등
        List<WorkRecordResponse> responses = employeeWorkRecordQueryService.findByMonth(userDetails.getId(), monthOffset);
        return ResponseEntity.ok(responses);
    }


    // 특정 기간 출근 기록
    @GetMapping("/period")
    public ResponseEntity<List<WorkRecordResponse>> period(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<WorkRecordResponse> responses = employeeWorkRecordQueryService.findByPeriod(userDetails.getId(), startDate, endDate);

        return ResponseEntity.ok(responses);
    }

}