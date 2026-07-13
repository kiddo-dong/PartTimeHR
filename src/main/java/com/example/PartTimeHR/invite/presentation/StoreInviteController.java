package com.example.PartTimeHR.invite.presentation;

import com.example.PartTimeHR.invite.application.InviteService;
import com.example.PartTimeHR.invite.presentation.dto.InviteCodeResponse;
import com.example.PartTimeHR.invite.presentation.dto.PendingEmployeeResponse;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 매장 초대코드 발급/관리 + 가입 승인 대기 (사장)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/invite")
@PreAuthorize("hasRole('EMPLOYER')")
public class StoreInviteController {

    private final InviteService inviteService;

    // 코드 조회 (없으면 발급) - 만료 없이 계속 쓰는 코드
    @GetMapping("/code")
    public ResponseEntity<InviteCodeResponse> getCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(inviteService.getOrCreateCode(userDetails.getId(), storeId));
    }

    // 재발급 (유출 등으로 기존 코드를 무효화하고 싶을 때)
    @PostMapping("/code/regenerate")
    public ResponseEntity<InviteCodeResponse> regenerateCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(inviteService.regenerateCode(userDetails.getId(), storeId));
    }

    // 승인 대기 중인 직원 목록
    @GetMapping("/pending")
    public ResponseEntity<List<PendingEmployeeResponse>> getPending(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(inviteService.getPendingEmployees(userDetails.getId(), storeId));
    }

    // 승인
    @PostMapping("/pending/{employeeId}/approve")
    public ResponseEntity<Void> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        inviteService.approve(userDetails.getId(), storeId, employeeId);
        return ResponseEntity.ok().build();
    }

    // 거절 (계정 삭제 - 이메일 재사용 가능해짐)
    @PostMapping("/pending/{employeeId}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {
        inviteService.reject(userDetails.getId(), storeId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
