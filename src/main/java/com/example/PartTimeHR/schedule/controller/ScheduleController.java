package com.example.PartTimeHR.schedule.controller;

import com.example.PartTimeHR.schedule.dto.CreateScheduleRequest;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@PreAuthorize("hasRole('EMPLOYER')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 직원 스케줄 생성
    @PostMapping("/{storeId}/{employeeId}")
    public ResponseEntity<Void> createSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestBody @Valid CreateScheduleRequest request
    ) {
        scheduleService.createSchedule(userDetails.getId(), storeId, employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 특정 직원 스케줄 조회
    @GetMapping("/{storeId}/{employeeId}")
    public ResponseEntity<List<ScheduleResponse>> getEmployeeSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                scheduleService.getEmployeeSchedules(userDetails.getId(), storeId, employeeId)
        );
    }

    // 가게 전체 스케줄 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<List<ScheduleResponse>> getStoreSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(
                scheduleService.getStoreSchedules(userDetails.getId(), storeId)
        );
    }

}
