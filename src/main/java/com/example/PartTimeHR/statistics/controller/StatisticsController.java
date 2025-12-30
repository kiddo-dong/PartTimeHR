package com.example.PartTimeHR.statistics.controller;

import com.example.PartTimeHR.statistics.dto.DashboardStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.EmployeeStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.MonthlyStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.WeeklyStatisticsResponse;
import com.example.PartTimeHR.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers/statistics")
@PreAuthorize("hasRole('EMPLOYER')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 대시보드 통계
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsResponse> getDashboardStatistics(@AuthenticationPrincipal String email) {

        DashboardStatisticsResponse response = statisticsService.getDashboardStatistics(email);
        return ResponseEntity.ok(response);
    }

    // 직원별 통계
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeStatisticsResponse> getEmployeeStatistics(
            @AuthenticationPrincipal String email,
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        EmployeeStatisticsResponse response = statisticsService.getEmployeeStatistics(email, employeeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // 월별 통계
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyStatisticsResponse> getMonthlyStatistics(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        MonthlyStatisticsResponse response = statisticsService.getMonthlyStatistics(email, year, month);
        return ResponseEntity.ok(response);
    }

    // 주간 통계
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyStatisticsResponse> getWeeklyStatistics(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        WeeklyStatisticsResponse response = statisticsService.getWeeklyStatistics(email, date);
        return ResponseEntity.ok(response);
    }
}

