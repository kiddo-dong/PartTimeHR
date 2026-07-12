package com.example.PartTimeHR.schedule.presentation;

import com.example.PartTimeHR.schedule.presentation.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.application.EmployeeScheduleService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employee/schedules")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
public class EmployeeScheduleController {
    private final EmployeeScheduleService employeeScheduleService;

    // 당일 스케줄 조회
    @GetMapping("/today")
    public ResponseEntity<List<ScheduleResponse>> getToday(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<ScheduleResponse> responses = employeeScheduleService.getTodaySchedule(userDetails.getId());
        return ResponseEntity.ok(responses);
    }

    // 기간별 스케줄 조회
    @GetMapping("/period")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByPeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("startDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam("endDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate
    ) {
        List<ScheduleResponse> responses = employeeScheduleService.getSchedulesByPeriod(
                userDetails.getId(), startDate, endDate
        );
        return ResponseEntity.ok(responses);
    }

    // 주간별 스케줄 조회
    @GetMapping("/week")
    public ResponseEntity<List<ScheduleResponse>> getWeekSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ){
        List<ScheduleResponse> responses = employeeScheduleService.getWeekSchedules(userDetails.getId(), offset);

        return ResponseEntity.ok(responses);
    }

    //월별 스케줄 조회
    @GetMapping("/month")
    public ResponseEntity<List<ScheduleResponse>> getMonthSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ){
        List<ScheduleResponse> responses = employeeScheduleService.getMonthSchedules(userDetails.getId(), offset);

        return ResponseEntity.ok(responses);
    }
}
