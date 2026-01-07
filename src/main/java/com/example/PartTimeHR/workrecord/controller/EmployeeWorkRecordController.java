package com.example.PartTimeHR.workrecord.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.workrecord.dto.EmployeeClockInRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployeeWorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// 사장님(고용주) 로그인 상태(JWT)에서 직원 출근/퇴근 원클릭으로 가능한 api
// 직원이 직접 이메일/비밀번호 입력 후 출근/휴게/퇴근 버튼 클릭
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/work-records")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployeeWorkRecordController {

    private final EmployeeWorkRecordService employeeWorkRecordService;

    // 직원 출근하기
    @PostMapping("/clock-in")
    public ResponseEntity<WorkRecordResponse> clockInByEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeClockInRequest request
    ) {

        WorkRecordResponse response = employeeWorkRecordService.clockInByEmployer(userDetails.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 직원 휴게 시작 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @PostMapping("/break-start")
    public ResponseEntity<WorkRecordResponse> startBreakByEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeClockInRequest request
    ) {

        WorkRecordResponse response = employeeWorkRecordService.startBreakByEmployer(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    // 직원 휴게 끝 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @PostMapping("/break-end")
    public ResponseEntity<WorkRecordResponse> endBreakByEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeClockInRequest request
    ) {

        WorkRecordResponse response = employeeWorkRecordService.endBreakByEmployer(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    // 직원 퇴근하기 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @PostMapping("/clock-out")
    public ResponseEntity<WorkRecordResponse> clockOutByEmployer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeClockInRequest request
    ) {

        WorkRecordResponse response = employeeWorkRecordService.clockOutByEmployer(userDetails.getEmail(), request);
        return ResponseEntity.ok(response);
    }
}
