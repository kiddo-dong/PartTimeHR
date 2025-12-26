package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.WorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers/work-records")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerWorkRecordController {

    private final WorkRecordService workRecordService;

    // 수동 등록
    @PostMapping
    public ResponseEntity<WorkRecordResponse> createWorkRecord(
            @Valid @RequestBody CreateWorkRecordRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.createWorkRecord(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 수정
    @PutMapping("/{recordId}")
    public ResponseEntity<WorkRecordResponse> updateWorkRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody UpdateWorkRecordRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.updateWorkRecord(recordId, email, request);
        return ResponseEntity.ok(response);
    }

    // 삭제
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteWorkRecord(@PathVariable Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        workRecordService.deleteWorkRecord(recordId, email);
        return ResponseEntity.noContent().build();
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<WorkRecordResponse>> getAllRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        List<WorkRecordResponse> response = workRecordService.getAllRecords(email, employeeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // 특정 기록 조회
    @GetMapping("/{recordId}")
    public ResponseEntity<WorkRecordResponse> getWorkRecord(@PathVariable Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        WorkRecordResponse response = workRecordService.getWorkRecord(recordId, email);
        return ResponseEntity.ok(response);
    }
}

