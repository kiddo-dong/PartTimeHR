package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.auth.domain.EmailVerification;
import com.example.PartTimeHR.auth.domain.PasswordResetToken;
import com.example.PartTimeHR.auth.repository.EmailVerificationRepository;
import com.example.PartTimeHR.auth.service.MailService;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.employer.repository.PasswordResetTokenRepository;
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

    // signup logic
    @Transactional
    public void signup(EmployerSignupRequest request) {

        // 이메일 중복 검사
        if (employerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Employer 생성
        Employer employer = Employer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .storeName(request.getStoreName())
                .weekStartDay(1)  // 기본값: 월요일
                .role(Role.ROLE_EMPLOYER)
                .emailVerified(false) // 이메일 인증 boolean
                .build();

        // 저장
        employerRepository.save(employer);

        // 이메일 인증 보내기
        EmailVerification ev = EmailVerification.create(employer); // Employer 객체 그대로 전달
        emailVerificationRepository.save(ev);
        mailService.sendEmail(
                employer.getEmail(),
                "이메일 인증",
                "아래 링크를 클릭해주세요:\n" +
                        "http://localhost:8080/api/auth/verify?token=" + ev.getToken()
        );
    }


    // 비밀번호 찾기 요청
    public void requestPasswordReset(String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일"));

        PasswordResetToken token = PasswordResetToken.create(employer);
        passwordResetTokenRepository.save(token);

        mailService.sendEmail(
                email,
                "비밀번호 재설정",
                "http://localhost:8080/api/employers/password/reset?token=" + token.getToken()
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
}
