package com.example.PartTimeHR.employer.controller;

import com.example.PartTimeHR.employer.dto.EmployerLoginRequest;
import com.example.PartTimeHR.employer.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employers")
public class EmployerLoginController {

    private final EmployerService employerService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody EmployerSignupRequest request
    ) {
        employerService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
