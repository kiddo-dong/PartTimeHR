package com.example.PartTimeHR.leave.presentation;

import com.example.PartTimeHR.leave.application.LeaveService;
import com.example.PartTimeHR.leave.domain.LeaveStatus;
import com.example.PartTimeHR.leave.presentation.dto.LeaveBalanceResponse;
import com.example.PartTimeHR.leave.presentation.dto.LeaveResponse;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 연차 관리 (사장)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/leaves")
@PreAuthorize("hasRole('EMPLOYER')")
public class LeaveController {

    private final LeaveService leaveService;

    // 신청 목록 (status 생략 시 전체)
    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getLeaves(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @RequestParam(required = false) LeaveStatus status
    ) {
        return ResponseEntity.ok(leaveService.getStoreLeaves(userDetails.getId(), storeId, status));
    }

    // 승인
    @PostMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveResponse> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long leaveId
    ) {
        return ResponseEntity.ok(leaveService.decide(userDetails.getId(), storeId, leaveId, true));
    }

    // 거절
    @PostMapping("/{leaveId}/reject")
    public ResponseEntity<LeaveResponse> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long leaveId
    ) {
        return ResponseEntity.ok(leaveService.decide(userDetails.getId(), storeId, leaveId, false));
    }

    // 직원 잔여 연차
    @GetMapping("/employees/{employeeId}/balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(leaveService.getBalance(userDetails.getId(), storeId, employeeId));
    }
}
