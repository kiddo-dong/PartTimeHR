package com.example.PartTimeHR.employer.controller;

import com.example.PartTimeHR.employer.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.dto.PasswordResetRequest;
import com.example.PartTimeHR.employer.service.EmployerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers")
public class EmployerAuthController {

    private final EmployerAuthService employerAuthService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody EmployerSignupRequest request
    ){
        employerAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
    @GetMapping("/password/reset-request")
    public ResponseEntity<String> findPassword(
        @Valid @RequestBody PasswordResetRequest request
    ){

    }
    */
}