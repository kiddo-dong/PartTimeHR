package com.example.PartTimeHR.workrecord.presentation;

import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.workrecord.application.WorkRecordService;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 직원 본인의 출근/휴게/퇴근 (JWT의 본인 id 기준)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/work-records")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeWorkRecordController {

    private final WorkRecordService workRecordService;

    // 출근
    @PostMapping("/clock-in")
    public ResponseEntity<WorkRecordResponse> clockIn(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WorkRecordResponse response = workRecordService.clockInSelf(userDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 휴게 시작
    @PostMapping("/break-start")
    public ResponseEntity<WorkRecordResponse> startBreak(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(workRecordService.startBreakSelf(userDetails.getId()));
    }

    // 휴게 종료
    @PostMapping("/break-end")
    public ResponseEntity<WorkRecordResponse> endBreak(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(workRecordService.endBreakSelf(userDetails.getId()));
    }

    // 퇴근
    @PostMapping("/clock-out")
    public ResponseEntity<WorkRecordResponse> clockOut(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(workRecordService.clockOutSelf(userDetails.getId()));
    }

    // 당일 근무 기록 조회
    @GetMapping("/today")
    public ResponseEntity<List<WorkRecordResponse>> getToday(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(workRecordService.getMyTodayRecords(userDetails.getId()));
    }
}
