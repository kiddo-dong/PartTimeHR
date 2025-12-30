package com.example.PartTimeHR.workrecord.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeClockInRequest {

    @Email
    @NotBlank
    private String email;  // 직원 이메일

    @NotBlank
    private String password;  // 직원 비밀번호
}

