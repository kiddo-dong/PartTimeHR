package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.WorkRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/work-records")
public class EmployeeWorkRecordController {

    private final WorkRecordService workRecordService;

    // 출근하기
    @PostMapping("/clock-in")
    public ResponseEntity<WorkRecordResponse> clockIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.clockIn(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 휴게 시작
    @PostMapping("/{recordId}/break-start")
    public ResponseEntity<WorkRecordResponse> startBreak(@PathVariable Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.startBreak(recordId, email);
        return ResponseEntity.ok(response);
    }

    // 휴게 끝
    @PostMapping("/{recordId}/break-end")
    public ResponseEntity<WorkRecordResponse> endBreak(@PathVariable Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.endBreak(recordId, email);
        return ResponseEntity.ok(response);
    }

    // 퇴근하기
    @PostMapping("/{recordId}/clock-out")
    public ResponseEntity<WorkRecordResponse> clockOut(@PathVariable Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.clockOut(recordId, email);
        return ResponseEntity.ok(response);
    }

    // 내 기록 조회
    @GetMapping
    public ResponseEntity<List<WorkRecordResponse>> getMyRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        List<WorkRecordResponse> response = workRecordService.getMyRecords(email, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // 오늘 기록 조회
    @GetMapping("/today")
    public ResponseEntity<WorkRecordResponse> getTodayRecord() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.getTodayRecord(email);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}

