package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.mail.domain.EmailVerification;
import com.example.PartTimeHR.mail.domain.PasswordResetToken;
import com.example.PartTimeHR.mail.repository.EmailVerificationRepository;
import com.example.PartTimeHR.mail.service.MailService;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.employer.repository.PasswordResetTokenRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerAuthService {

    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailService mailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PayPolicyRepository payPolicyRepository;

    // signup logic
    // 회원가입
    @Transactional
    public void signup(EmployerSignupRequest request) {

        // 1. 이메일 중복 체크
        if (employerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. Employer 생성
        Employer employer = Employer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .storeName(request.getStoreName())
                .weekStartDay(1)
                .role(Role.ROLE_EMPLOYER)
                .emailVerified(false)
                .weeklyPayApplicable(request.isWeeklyPayApplicable())
                .build();
        employerRepository.save(employer);

        // 4. 기본 PayPolicy 생성
        PayPolicy defaultPolicy = PayPolicy.builder()
                .employer(employer)
                .jobTitle("알바생")
                .hourlyWage(10320)
                .isDefault(true)
                .active(true)
                .build();
        payPolicyRepository.save(defaultPolicy);

        // 5. 이메일 인증 생성
        EmailVerification ev = EmailVerification.create(employer);
        emailVerificationRepository.save(ev);

        // 6. HTML 이메일 생성
        String html = createVerificationEmailHtml(employer.getName(), ev.getToken());

        // 7. HTML 메일 발송
        mailService.sendHtmlEmail(employer.getEmail(), "이메일 인증", html);
    }


    // 비밀번호 찾기 요청
    public void requestPasswordReset(String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일"));

        PasswordResetToken token = PasswordResetToken.create(employer);
        passwordResetTokenRepository.save(token);

        // HTML 이메일 생성
        String html = createPasswordResetEmailHtml(employer.getName(), token.getToken());

        // HTML 메일 발송
        mailService.sendHtmlEmail(
                email,
                "비밀번호 재설정",
                html
        );
    }


    // 토큰 검증
    public Employer verifyPasswordResetToken(String token) {
        PasswordResetToken t = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰"));

        if (t.isExpired()) throw new IllegalArgumentException("만료된 토큰");

        return t.getEmployer();
    }

    // 새 비밀번호 저장
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        Employer employer = verifyPasswordResetToken(token);

        employer.setPassword(passwordEncoder.encode(newPassword));
    }

    // 계정 생성용 이메일 인증 템플릿
    private String createVerificationEmailHtml(String name, String token) {
        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + token;

        return "<!DOCTYPE html>"
                + "<html lang='ko'>"
                + "<head>"
                + "  <meta charset='UTF-8'>"
                + "  <title>이메일 인증</title>"
                + "  <style>"
                + "    .btn {"
                + "      display: inline-block;"
                + "      padding: 12px 24px;"
                + "      background-color: #4CAF50;"
                + "      color: white;"
                + "      text-decoration: none;"
                + "      border-radius: 5px;"
                + "      font-weight: bold;"
                + "    }"
                + "    .container {"
                + "      font-family: Arial, sans-serif;"
                + "      text-align: center;"
                + "      padding: 20px;"
                + "    }"
                + "    .footer {"
                + "      margin-top: 30px;"
                + "      font-size: 12px;"
                + "      color: #777;"
                + "    }"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <div class='container'>"
                + "    <h2>안녕하세요, " + name + "님!</h2>"
                + "    <p>회원가입을 완료하려면 아래 버튼을 클릭해주세요.</p>"
                + "    <a class='btn' href='" + verifyLink + "'>이메일 인증하기</a>"
                + "    <p class='footer'>버튼이 작동하지 않으면 아래 링크를 브라우저에 붙여넣으세요:<br>"
                + verifyLink
                + "    </p>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    // 비밀번호 리셋을 위한 이메일 인증 템플릿
    private String createPasswordResetEmailHtml(String name, String token) {
        String resetLink = "http://localhost:8080/api/employers/password/reset?token=" + token;

        return "<!DOCTYPE html>"
                + "<html lang='ko'>"
                + "<head>"
                + "  <meta charset='UTF-8'>"
                + "  <title>비밀번호 재설정</title>"
                + "  <style>"
                + "    .btn { display:inline-block; padding:12px 24px; background-color:#f44336; color:white;"
                + " text-decoration:none; border-radius:5px; font-weight:bold; }"
                + "    .container { font-family: Arial, sans-serif; text-align:center; padding:20px; }"
                + "    .footer { margin-top:20px; font-size:12px; color:#777; }"
                + "  </style>"
                + "</head>"
                + "<body>"
                + "  <div class='container'>"
                + "    <h2>안녕하세요, " + name + "님!</h2>"
                + "    <p>비밀번호 재설정을 원하시면 아래 버튼을 클릭해주세요.</p>"
                + "    <a class='btn' href='" + resetLink + "'>비밀번호 재설정</a>"
                + "    <p class='footer'>버튼이 작동하지 않으면 아래 링크를 브라우저에 붙여넣으세요:<br>"
                + resetLink
                + "    </p>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }
}
