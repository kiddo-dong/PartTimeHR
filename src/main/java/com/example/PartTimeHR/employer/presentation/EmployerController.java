package com.example.PartTimeHR.employer.presentation;

import com.example.PartTimeHR.employer.presentation.dto.*;
import com.example.PartTimeHR.employer.application.EmployerService;
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
@RequestMapping("/api/employers")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerController {

    private final EmployerService employerService;

    // 본인 정보 조회(사장님)
    @GetMapping("/me")
    public ResponseEntity<EmployerInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // jwt 인증 정보로 find
        EmployerInfoResponse response = employerService.getMyInfo(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    // Update (사장님 정보 수정)
    @PutMapping
    public ResponseEntity<EmployerInfoResponse> updateInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateEmployerRequest request
    ) {
        EmployerInfoResponse response = employerService.updateInfo(userDetails.getId(), request);

        return ResponseEntity.ok(response);
    }
}