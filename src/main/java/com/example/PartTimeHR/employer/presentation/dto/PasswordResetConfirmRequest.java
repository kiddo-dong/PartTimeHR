package com.example.PartTimeHR.employer.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 비밀번호 재설정 확정 (비밀번호는 로그에 남지 않도록 body로 받는다)
@Getter
@NoArgsConstructor
public class PasswordResetConfirmRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 20)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 20)
    private String newPasswordConfirm;
}
