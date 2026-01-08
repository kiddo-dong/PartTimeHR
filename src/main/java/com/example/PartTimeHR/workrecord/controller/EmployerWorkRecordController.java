package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployerWorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 사장 전용 (관리 + 조회)
// 사장님(고용주)이 직접 생성/수정/삭제 및 조회가 가능한 api
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers/work-records")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerWorkRecordController {

    private final EmployerWorkRecordService employerWorkRecordService;

    // ===== Create/Update/Delete API =====
    // 수동 등록
    @PostMapping
    public ResponseEntity<WorkRecordResponse> createWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateWorkRecordRequest request
    ) {

        WorkRecordResponse response = employerWorkRecordService.createWorkRecord(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 수정
    @PutMapping("/{recordId}")
    public ResponseEntity<WorkRecordResponse> updateWorkRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId,
            @Valid @RequestBody UpdateWorkRecordRequest request
    ) {
        WorkRecordResponse response = employerWorkRecordService.updateWorkRecord(recordId, userDetails.getId(), request);
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

    // ===== READ API =====

    // 가게 전체 - 당일
    @GetMapping("employees/today")
    public ResponseEntity<List<WorkRecordResponse>> getTodayRecords(){
        List<WorkRecordResponse> response = employerWorkRecordService.todayWorkRecords();
        return ResponseEntity.ok(response);
    }

    // 가게 전체 - 주간
    @GetMapping("employees/week")
    public ResponseEntity<List<WorkRecordResponse>> storeWeek(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(
                employerWorkRecordService.findStoreWeek(
                        userDetails.getId(),
                        offset
                )
        );
    }

    // 가게 전체 - 월별
    @GetMapping("employees/month")
    public ResponseEntity<List<WorkRecordResponse>> storeMonth(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(
                employerWorkRecordService.findStoreMonth(
                        userDetails.getId(),
                        offset
                )
        );
    }

    // 특정 직원 - 주간
    @GetMapping("/employees/week/{employeeId}")
    public ResponseEntity<List<WorkRecordResponse>> employeeWeek(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(
                employerWorkRecordService.findEmployeeWeek(
                        userDetails.getId(),
                        employeeId,
                        offset
                )
        );
    }

    // 특정 직원 - 월간
    @GetMapping("/employees/month/{employeeId}")
    public ResponseEntity<List<WorkRecordResponse>> employeeMonth(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(
                employerWorkRecordService.findEmployeeMonth(
                        userDetails.getId(),
                        employeeId,
                        offset
                )
        );
    }

    // 특정 직원 - 기간
    @GetMapping("/employees/period/{employeeId}")
    public ResponseEntity<List<WorkRecordResponse>> employeePeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(
                employerWorkRecordService.findByPeriod(
                        userDetails.getId(),
                        employeeId,
                        startDate,
                        endDate
                )
        );
    }
}