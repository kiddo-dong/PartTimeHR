package com.example.PartTimeHR.mail.presentation;

import com.example.PartTimeHR.mail.application.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);

        return "이메일 인증 완료";
    }

    @PostMapping("/resend")
    public String resend(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);

        return "재발송 완료";
    }
}
