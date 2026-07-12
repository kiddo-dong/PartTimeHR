package com.example.PartTimeHR.paypolicy.presentation;

import com.example.PartTimeHR.paypolicy.presentation.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.application.PayPolicyService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/paypolicies")
@PreAuthorize("hasRole('EMPLOYER')")
public class PayPolicyController {

    private final PayPolicyService payPolicyService;

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