package com.example.PartTimeHR.mail.controller;

import com.example.PartTimeHR.mail.service.EmailVerificationService;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final EmployerRepository employerRepository;

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {

        emailVerificationService.verifyEmail(token); // DB 반영!

        return "이메일 인증 완료";
    }


    @PostMapping("/resend")
    public String resend(@RequestParam String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일"));

        emailVerificationService.resendVerificationEmail(employer);
        return "재발송 완료";
    }

}
