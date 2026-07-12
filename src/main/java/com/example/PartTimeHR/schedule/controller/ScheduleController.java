package com.example.PartTimeHR.schedule.controller;

import com.example.PartTimeHR.schedule.dto.ScheduleCreateRequest;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.dto.ScheduleUpdateRequest;
import com.example.PartTimeHR.schedule.service.ScheduleService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 기능 추가 필요
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/schedules")
@PreAuthorize("hasRole('EMPLOYER')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ===== 스케줄 생성 =====
    @PostMapping
    public ResponseEntity<Void> createSchedule(
            @PathVariable Long storeId,
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        scheduleService.createSchedule(storeId, userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ===== 스케줄 수정 =====
    @PutMapping("/{scheduleId}/employees/{employeeId}")
    public ResponseEntity<Void> updateSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        scheduleService.updateSchedule(
                userDetails.getId(),
                storeId,
                employeeId,
                scheduleId,
                request
        );
        return ResponseEntity.ok().build();
    }

    // ===== 전체 직원 조회(단일/기간/주간/월간) =====
    // 전체 날짜별 스케줄 조회
    @GetMapping("/date")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @PathVariable Long storeId,
            @RequestParam @NotNull LocalDate workDate,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ScheduleResponse> responses = scheduleService.getSchedules(storeId, userDetails.getId(), workDate);

        return ResponseEntity.ok(responses);
    }

    // 전체 기간별 스케줄 조회
    @GetMapping("/period")
    public List<ScheduleResponse> getStoreSchedules(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return scheduleService.findStoreSchedulesByPeriod(
                        userDetails.getId(), storeId, startDate, endDate
        );
    }

    // 전체 주간별 스케줄 조회
    @GetMapping("/week")
    public ResponseEntity<List<ScheduleResponse>> getWeekSchedules(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                scheduleService.getWeekSchedules(
                        storeId,
                        userDetails.getId(),
                        offset
                )
        );
    }

    // 전체 월별 스케줄 조회
    @GetMapping("/month")
    public ResponseEntity<List<ScheduleResponse>> getMonthSchedules(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                scheduleService.getMonthSchedules(
                        storeId,
                        userDetails.getId(),
                        offset
                )
        );
    }

    // ===== 직원별 조회(단일/기간/주간/월간) =====
    // 직원별 단일 스케줄 조회
    @GetMapping("employees/{employeeId}/date")
    public List<ScheduleResponse> getEmployeeScheduleByDate(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return scheduleService.findEmployeeSchedulesByDate(
                userDetails.getId(), storeId, employeeId, date
        );
    }

    // 직원별 기간별 스케줄 조회
    @GetMapping("employees/{employeeId}/period")
    public List<ScheduleResponse> getEmployeeSchedules(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return scheduleService.findEmployeeSchedulesByPeriod(
                userDetails.getId(), employeeId, storeId, startDate, endDate
        );
    }


    // 직원별 주간별 스케줄 조회
    @GetMapping("/employees/{employeeId}/week")
    public ResponseEntity<List<ScheduleResponse>> getEmployeeWeekSchedules(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                scheduleService.getEmployeeWeekSchedules(
                        storeId,
                        userDetails.getId(),
                        employeeId,
                        offset
                )
        );
    }

    // 직원별 월별 스케줄 조회
    @GetMapping("/employees/{employeeId}/month")
    public ResponseEntity<List<ScheduleResponse>> getEmployeeMonthSchedules(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                scheduleService.getEmployeeMonthSchedules(
                        storeId,
                        userDetails.getId(),
                        employeeId,
                        offset
                )
        );
    }

    // ===== 직급별 조회 추가 예정(단일/기간/주간/월간) =====

    // ===== 스케줄 삭제 =====
    @DeleteMapping("/{scheduleId}/employees/{employeeId}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @PathVariable Long scheduleId
    ) {
        scheduleService.deleteSchedule(
                userDetails.getId(),
                storeId,
                employeeId,
                scheduleId
        );

        return ResponseEntity.noContent().build();
    }
}
