package com.example.PartTimeHR.invite.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 직원이 매장 초대 코드로 스스로 가입할 때 보내는 요청 (인증 없이 호출)
@Getter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank
    private String code;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    @Size(min = 8, max = 20)
    private String passwordConfirm;

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String phone;
}
