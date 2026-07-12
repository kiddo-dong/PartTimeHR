package com.example.PartTimeHR.employer.application;

import com.example.PartTimeHR.mail.domain.EmailVerification;
import com.example.PartTimeHR.mail.domain.PasswordResetToken;
import com.example.PartTimeHR.mail.domain.EmailVerificationRepository;
import com.example.PartTimeHR.mail.application.MailService;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.presentation.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import com.example.PartTimeHR.employer.domain.PasswordResetTokenRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerAuthService {

    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailService mailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final StoreRepository storeRepository;

    // signup logic
    // 회원가입
    @Transactional
    public void signup(EmployerSignupRequest request) {

        // 직원 계정과도 이메일이 겹치면 안 됨 (로그인이 이메일 하나로 이뤄짐)
        if (employerRepository.existsByEmail(request.getEmail())
                || employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 1. Employer 생성 (인증용)
        Employer employer = Employer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(Role.ROLE_EMPLOYER)
                .emailVerified(false)
                .build();
        employerRepository.save(employer);

        // 2. findStore 생성
        Store store = Store.builder()
                .name(request.getStoreName())
                .phone(request.getStorePhone())
                .address(request.getStoreAddress())
                .weekStartDay(request.getWeekStartDay())
                .weeklyPayApplicable(request.getWeeklyPayApplicable())
                .employer(employer)
                .build();
        storeRepository.save(store);

        // 3. 기본 PayPolicy 생성 (findStore 기준)
        PayPolicy defaultPolicy = PayPolicy.builder()
                .store(store)
                .jobTitle("알바생")
                .hourlyWage(10320)
                .isDefault(true)
                .active(true)
                .build();
        payPolicyRepository.save(defaultPolicy);

        // 4. 이메일 인증
        EmailVerification ev = EmailVerification.create(employer);
        emailVerificationRepository.save(ev);

        mailService.sendHtmlEmail(
                employer.getEmail(),
                "이메일 인증",
                createVerificationEmailHtml(employer.getName(), ev.getToken())
        );
    }


    // 비밀번호 찾기 요청
    @Transactional
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
    @Transactional(readOnly = true)
    public Employer verifyPasswordResetToken(String token) {
        return findValidToken(token).getEmployer();
    }

    // 새 비밀번호 저장
    @Transactional
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        PasswordResetToken resetToken = findValidToken(token);

        Employer employer = resetToken.getEmployer();
        employer.setPassword(passwordEncoder.encode(newPassword));

        // 재사용 방지 (1회용 토큰)
        passwordResetTokenRepository.delete(resetToken);
    }

    private PasswordResetToken findValidToken(String token) {
        PasswordResetToken t = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰"));

        if (t.isExpired()) throw new IllegalArgumentException("만료된 토큰");

        return t;
    }

    // 계정 생성용 이메일 인증 템플릿
    private String createVerificationEmailHtml(String name, String token) {
        String verifyLink = "http://localhost:8080/api/email/verify?token=" + token;

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
