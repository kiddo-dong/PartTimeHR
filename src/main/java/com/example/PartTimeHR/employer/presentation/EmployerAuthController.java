package com.example.PartTimeHR.employer.presentation;

import com.example.PartTimeHR.employer.presentation.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.presentation.dto.PasswordResetConfirmRequest;
import com.example.PartTimeHR.employer.presentation.dto.PasswordResetRequest;
import com.example.PartTimeHR.employer.application.EmployerAuthService;
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

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody EmployerSignupRequest request
    ){
        employerAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 비밀번호 리셋 요청 -> 메일 발송
    @PostMapping("/password/reset-request")
    public ResponseEntity<String> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ){
        employerAuthService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("메일 발송 완료");
    }

    // 비밀번호 리셋 (비밀번호가 서버 로그에 남지 않도록 body로 받는다)
    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ){
        employerAuthService.resetPassword(
                request.getToken(),
                request.getNewPassword(),
                request.getNewPasswordConfirm()
        );
        return ResponseEntity.ok("비밀번호 재설정 완료");
    }

}