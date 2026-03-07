package com.example.PartTimeHR.attendance.controller;

import com.example.PartTimeHR.attendance.dto.AttendanceDailyResponse;
import com.example.PartTimeHR.attendance.dto.AttendanceSummaryResponse;
import com.example.PartTimeHR.attendance.service.AttendanceService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/attendance")
@PreAuthorize("hasRole('EMPLOYER')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/daily")
    public ResponseEntity<AttendanceDailyResponse> getDailyAttendance(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AttendanceDailyResponse response = attendanceService.getDailyAttendance(employer.getId(), storeId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AttendanceSummaryResponse response = attendanceService.getSummary(employer.getId(), storeId, from, to);
        return ResponseEntity.ok(response);
    }
}
