package com.example.PartTimeHR.leave.presentation;

import com.example.PartTimeHR.leave.application.LeaveService;
import com.example.PartTimeHR.leave.presentation.dto.LeaveBalanceResponse;
import com.example.PartTimeHR.leave.presentation.dto.LeaveCreateRequest;
import com.example.PartTimeHR.leave.presentation.dto.LeaveResponse;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 연차 신청/조회 (직원 본인)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/leaves")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeLeaveController {

    private final LeaveService leaveService;

    // 연차 신청
    @PostMapping
    public ResponseEntity<LeaveResponse> request(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LeaveCreateRequest request
    ) {
        LeaveResponse response = leaveService.request(userDetails.getId(), request.getLeaveDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 신청 내역
    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(leaveService.getMyLeaves(userDetails.getId()));
    }

    // 잔여 연차
    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getMyBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(leaveService.getMyBalance(userDetails.getId()));
    }
}
