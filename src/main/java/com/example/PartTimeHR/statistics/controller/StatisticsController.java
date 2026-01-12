package com.example.PartTimeHR.statistics.controller;

import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.statistics.dto.EmployeeMonthWorkStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.EmployeeWeekWorkStatisticsResponse;
import com.example.PartTimeHR.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 가게 전체 - 주간 통계
    @GetMapping("/week")
    public ResponseEntity<EmployeeWeekWorkStatisticsResponse> storeWeekStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ) {
        EmployeeWeekWorkStatisticsResponse response = statisticsService.getEmployeeWeekWorkStatistics(userDetails.getId(), offset);

        return ResponseEntity.ok(response);
    }

    // 가게 전체 - 월간 통계
    @GetMapping("/month")
    public ResponseEntity<EmployeeMonthWorkStatisticsResponse> storeMonthStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ){
        EmployeeMonthWorkStatisticsResponse response = statisticsService.getEmployeeMonthWorkStatistics(userDetails.getId(), offset);
        return ResponseEntity.ok(response);
    }

}
