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
            ev = EmailVerification.create(employer);
            emailVerificationRepository.save(ev);
        }

        // HTML 이메일 생성
        String html = "<!DOCTYPE html>"
                + "<html lang='ko'>"
                + "<head>"
                + "  <meta charset='UTF-8'>"
                + "  <title>이메일 인증</title>"
                + "  <style>"
                + "    .btn { display:inline-block;padding:12px 24px;background-color:#4CAF50;color:white;"
                + "text-decoration:none;border-radius:5px;font-weight:bold; }"
                + "    .container { font-family: Arial,sans-serif; text-align:center; padding:20px; }"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <div class='container'>"
                + "    <h2>안녕하세요, " + employer.getName() + "님!</h2>"
                + "    <p>회원가입을 완료하려면 아래 버튼을 클릭해주세요.</p>"
                + "    <a class='btn' href='http://localhost:8080/api/auth/verify?token=" + ev.getToken() + "'>이메일 인증하기</a>"
                + "    <p>버튼이 작동하지 않으면 아래 링크를 브라우저에 붙여넣으세요:<br>"
                + "http://localhost:8080/api/auth/verify?token=" + ev.getToken() + "</p>"
                + "  </div>"
                + "</body>"
                + "</html>";

        mailService.sendHtmlEmail(employer.getEmail(), "이메일 인증 재발송", html);
    }


}
