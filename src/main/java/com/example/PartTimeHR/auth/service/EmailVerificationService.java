package com.example.PartTimeHR.auth.service;

import com.example.PartTimeHR.auth.domain.EmailVerification;
import com.example.PartTimeHR.auth.repository.EmailVerificationRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MailService mailService;

    public void verifyEmail(String token) {
        EmailVerification ev = emailVerificationRepository.findByTokenWithEmployer(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰"));

        if (ev.isExpired()) {
            throw new IllegalArgumentException("만료된 토큰");
        }

        ev.getEmployer().verifyEmail();
    }

    public void resendVerificationEmail(Employer employer) {
        // 기존 토큰 조회
        EmailVerification ev = emailVerificationRepository.findByEmployerId(employer.getId())
                .orElse(null);

        if (ev == null || ev.isExpired()) {
            // 새 토큰 생성
            ev = EmailVerification.create(employer);
            emailVerificationRepository.save(ev);
        }

        // 메일 발송
        mailService.sendEmail(
                employer.getEmail(),
                "이메일 인증 재발송",
                "http://localhost:8080/api/auth/verify?token=" + ev.getToken()
        );
    }

}
