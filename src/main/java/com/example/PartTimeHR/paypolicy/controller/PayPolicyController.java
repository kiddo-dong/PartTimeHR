package com.example.PartTimeHR.paypolicy.controller;

import com.example.PartTimeHR.paypolicy.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.service.PayPolicyService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store/{storeId}/paypolicies")
@PreAuthorize("hasRole('EMPLOYER')")
public class PayPolicyController {

    private final PayPolicyService payPolicyService;

    @PostMapping
    public ResponseEntity<Void> createPayPolicy(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePayPolicyRequest request
    ) {
        payPolicyService.createPayPolicy(
                storeId,
                userDetails.getId(),
                request
        );

        return ResponseEntity.ok().build();
    }
}
