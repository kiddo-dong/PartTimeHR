package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.WorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 직원 출근/휴게/퇴근 (사장 컨텍스트)
// JWT 기반 직원 출근/퇴근 원클릭으로 가능한 api
// 직원이 직접 출근/휴게/퇴근 버튼 클릭
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/work-records")
@PreAuthorize("hasRole('EMPLOYER')")
public class WorkRecordController {

    private final WorkRecordService workRecordService;

    // ===== 자동 생성(원클릭) =====
    // 출근
    @PostMapping("/employees/{employeeId}/clock-in")
    public ResponseEntity<WorkRecordResponse> clockIn(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        WorkRecordResponse response = workRecordService.clockIn(employer.getId(), storeId, employeeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 휴게 시작
    @PostMapping("/employees/{employeeId}/break-start")
    public ResponseEntity<WorkRecordResponse> startBreak(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        WorkRecordResponse response = workRecordService.startBreak(employer.getId(), storeId, employeeId);

        return ResponseEntity.ok(response);
    }

    // 휴게 끝
    @PostMapping("/employees/{employeeId}/break-end")
    public ResponseEntity<WorkRecordResponse> endBreak(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                workRecordService.endBreak(
                        employer.getId(), storeId, employeeId
                )
        );
    }

    // 퇴근
    @PostMapping("/employees/{employeeId}/clock-out")
    public ResponseEntity<WorkRecordResponse> clockOut(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                workRecordService.clockOut(
                        employer.getId(), storeId, employeeId
                )
        );
    }

    // ===== 수동 생성 =====
    // 수동 근무 기록 생성
    @PostMapping("employees/manual")
    public ResponseEntity<WorkRecordResponse> createManual(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @Valid @RequestBody CreateWorkRecordRequest request
    ) {
        WorkRecordResponse response =
                workRecordService.createManual(
                        employer.getId(), storeId, request
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== 수정 =====
    @PutMapping("/{workRecordId}")
    public ResponseEntity<WorkRecordResponse> updateWorkRecord(
            @AuthenticationPrincipal CustomUserDetails employer,
            @PathVariable Long storeId,
            @PathVariable Long workRecordId,
            @Valid @RequestBody UpdateWorkRecordRequest request
    ) {
        WorkRecordResponse response =
                workRecordService.updateWorkRecord(
                        employer.getId(), storeId, workRecordId, request
                );

        return ResponseEntity.ok(response);
    }
}