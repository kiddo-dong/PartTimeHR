package com.example.PartTimeHR.schedule.controller;

import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.service.EmployeeScheduleService;
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

    @GetMapping("/week")
    public ResponseEntity<List<ScheduleResponse>> getWeekSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ){
        List<ScheduleResponse> responses = employeeScheduleService.getWeekSchedules(userDetails.getId(), offset);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/month")
    public ResponseEntity<List<ScheduleResponse>> getMonthSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ){
        List<ScheduleResponse> responses = employeeScheduleService.getMonthSchedules(userDetails.getId(), offset);

        return ResponseEntity.ok(responses);
    }
}
