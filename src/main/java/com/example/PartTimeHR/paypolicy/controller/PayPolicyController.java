package com.example.PartTimeHR.paypolicy.controller;

import com.example.PartTimeHR.global.security.CustomUserDetails;
import com.example.PartTimeHR.paypolicy.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.dto.UpdatePayPolicyResponse;
import com.example.PartTimeHR.paypolicy.service.PayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paypolicy")
@RequiredArgsConstructor
public class PayPolicyController {

    private final PayPolicyService payPolicyService;

    @PutMapping("/employee/{employeeId}")
    public ResponseEntity<UpdatePayPolicyResponse> updateEmployeePolicy(
            @PathVariable Long employeeId,
            @RequestBody UpdatePayPolicyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UpdatePayPolicyResponse response = payPolicyService.updateEmployeePolicy(
                employeeId,
                userDetails.getEmail(),
                request
        );
        return ResponseEntity.ok(response);
    }

}
