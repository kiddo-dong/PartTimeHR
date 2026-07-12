package com.example.PartTimeHR.paypolicy.presentation;

import com.example.PartTimeHR.paypolicy.presentation.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.presentation.dto.PayPolicyResponse;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.application.PayPolicyService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/paypolicies")
@PreAuthorize("hasRole('EMPLOYER')")
public class PayPolicyController {

    private final PayPolicyService payPolicyService;

    // 조회 (매장의 직급/시급 목록)
    @GetMapping
    public ResponseEntity<List<PayPolicyResponse>> getPayPolicies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(
                payPolicyService.getPayPolicies(storeId, userDetails.getId())
        );
    }

    // 생성
    @PostMapping
    public ResponseEntity<Void> createPayPolicy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @Valid @RequestBody CreatePayPolicyRequest request
    ) {
        payPolicyService.createPayPolicy(
                storeId,
                userDetails.getId(),
                request
        );

        return ResponseEntity.ok().build();
    }

    // 수정
    @PutMapping("/{payPolicyId}")
    public ResponseEntity<Void> updatePayPolicy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long payPolicyId,
            @Valid @RequestBody UpdatePayPolicyRequest request
    ) {
        payPolicyService.updatePayPolicy(storeId, payPolicyId, userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }
}