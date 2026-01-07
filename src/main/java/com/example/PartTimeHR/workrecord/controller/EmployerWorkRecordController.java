package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.EmployeeClockInRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployerWorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 사장님(고용주)이 직접 생성/수정/삭제 및 조회가 가능한 api
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers/work-records")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerWorkRecordController {

    private final EmployerWorkRecordService employerWorkRecordService;

    // 수동 등록
    @PostMapping
    public ResponseEntity<WorkRecordResponse> createWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateWorkRecordRequest request
    ) {

        WorkRecordResponse response = employerWorkRecordService.createWorkRecord(userDetails.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 수정
    @PutMapping("/{recordId}")
    public ResponseEntity<WorkRecordResponse> updateWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId,
            @Valid @RequestBody UpdateWorkRecordRequest request
    ) {
        WorkRecordResponse response = employerWorkRecordService.updateWorkRecord(recordId, userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    // 삭제
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId
    ){

        employerWorkRecordService.deleteWorkRecord(recordId, userDetails.getEmail());
        return ResponseEntity.noContent().build();
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<WorkRecordResponse>> getAllRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        List<WorkRecordResponse> response = employerWorkRecordService.getAllRecords(userDetails.getEmail(), employeeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // 특정 기록 조회 (이미 존재하는 출퇴근 기록의 id값(PK) )
    @GetMapping("/{recordId}")
    public ResponseEntity<WorkRecordResponse> getWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId
    ) {

        WorkRecordResponse response = employerWorkRecordService.getWorkRecord(recordId, userDetails.getEmail());
        return ResponseEntity.ok(response);
    }

    // 오늘 출퇴근 기록 조회
    @PostMapping("/today")
    public ResponseEntity<WorkRecordResponse> getTodayRecordByEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeClockInRequest request
    ) {
        WorkRecordResponse response = employerWorkRecordService.getTodayRecordByEmployer(userDetails.getEmail(), request);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }


}