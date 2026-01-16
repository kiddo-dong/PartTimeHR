package com.example.PartTimeHR.extrawork.controller;

import com.example.PartTimeHR.extrawork.dto.CreateExtraWorkRequest;
import com.example.PartTimeHR.extrawork.service.ExtraWorkService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/employees/{employeeId}/extra-works")
@PreAuthorize("hasRole('EMPLOYER')")
public class ExtraWorkController {

    private final ExtraWorkService extraWorkService;

    @PostMapping
    public ResponseEntity<Void> createExtraWork(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestBody @Valid CreateExtraWorkRequest request
    ) {
        extraWorkService.create(
                userDetails.getId(),
                storeId,
                employeeId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
